package com.example.agent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.Semaphore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AgentApplication {
    private static final int HEARTBEAT_INTERVAL_SECONDS = 10;
    private static final String SERVER_URL = "http://localhost:8080/api";
    
    // Настройки выполнения команд
    private static final int COMMAND_TIMEOUT_SECONDS = 30;
    private static final int MAX_OUTPUT_LINES = 500;
    private static final int MAX_OUTPUT_CHARS = 10000;
    private static final int MAX_CONCURRENT_COMMANDS = 5;
    private static final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_COMMANDS);
    
    public static void main(String[] args) {
        System.out.println("=== Remote Admin Agent ===");
        
        Config config = new Config();
        SystemMonitor monitor = new SystemMonitor();
        ApiClient apiClient = new ApiClient();
        
        String computerName = monitor.getComputerName();
        String ip = "127.0.0.1";
        
        Long agentId;
        
        try {
            if (config.hasAgentId()) {
                agentId = Long.parseLong(config.getAgentId());
                System.out.println("Загружен agentId: " + agentId);
            } else {
                System.out.println("Регистрация нового агента...");
                String idStr = apiClient.registerAgent(computerName, ip);
                agentId = Long.parseLong(idStr);
                config.setAgentId(idStr);
                System.out.println("Зарегистрирован с ID: " + agentId);
            }
            
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    double cpuLoad = monitor.getCpuLoad();
                    int freeRam = monitor.getFreeRamMb();
                    
                    System.out.printf("Heartbeat [ID: %d] CPU: %.1f%% RAM: %d MB%n", 
                            agentId, cpuLoad, freeRam);
                    
                    apiClient.sendHeartbeat(agentId, cpuLoad, freeRam);
                    
                    checkAndExecuteCommands(agentId, apiClient);
                } catch (Exception e) {
                    System.err.println("Ошибка: " + e.getMessage());
                }
            }, 0, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
            
            System.out.println("Agent работает. Нажми Ctrl+C для остановки.");
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void checkAndExecuteCommands(Long agentId, ApiClient apiClient) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/commands/pending?agentId=" + agentId))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode commands = mapper.readTree(response.body());

                if (commands.isArray() && commands.size() > 0) {
                    System.out.println("Найдено команд для выполнения: " + commands.size());

                    for (JsonNode cmdNode : commands) {
                        final Long commandId = cmdNode.get("id").asLong();
                        final String commandText = cmdNode.get("commandText").asText();
                        int timeout = cmdNode.has("timeoutSeconds") ? 
                            cmdNode.get("timeoutSeconds").asInt() : COMMAND_TIMEOUT_SECONDS;
                        final int finalTimeout = timeout;

                        Thread commandThread = new Thread(() -> {
                            try {
                                semaphore.acquire();
                                System.out.println("Выполняю команду ID " + commandId + ": " + commandText);
                                System.out.println("Таймаут: " + finalTimeout + " сек.");
                                
                                String result = executeCommand(commandText, finalTimeout);
                                apiClient.sendCommandResult(commandId, result);
                            } catch (Exception e) {
                                System.err.println("Ошибка при выполнении команды " + commandId + ": " + e.getMessage());
                            } finally {
                                semaphore.release();
                            }
                        });
                        commandThread.start();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при проверке/выполнении команд: " + e.getMessage());
        }
    }

    private static String executeCommand(String command, int timeoutSeconds) {
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
        final AtomicReference<Process> processRef = new AtomicReference<>();
        
        try {
            String[] cmdArray;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                cmdArray = new String[]{"cmd", "/c", command};
            } else {
                cmdArray = new String[]{"/bin/sh", "-c", command};
            }
            
            System.out.println("Запуск: " + String.join(" ", cmdArray));
            Process process = Runtime.getRuntime().exec(cmdArray);
            processRef.set(process);
            
            // Потоки для чтения вывода в реальном времени
            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(processRef.get().getInputStream(), "CP866"))) {
                    String line;
                    int lineCount = 0;
                    while ((line = reader.readLine()) != null && lineCount < MAX_OUTPUT_LINES) {
                        synchronized (output) {
                            output.append(line).append("\n");
                        }
                        lineCount++;
                    }
                    if (lineCount >= MAX_OUTPUT_LINES) {
                        synchronized (output) {
                            output.append("\n... (вывод обрезан, лимит ").append(MAX_OUTPUT_LINES).append(" строк)\n");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка чтения stdout: " + e.getMessage());
                }
            });
            
            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(processRef.get().getErrorStream(), "CP866"))) {
                    String line;
                    int lineCount = 0;
                    while ((line = reader.readLine()) != null && lineCount < MAX_OUTPUT_LINES) {
                        synchronized (errorOutput) {
                            errorOutput.append("ERROR: ").append(line).append("\n");
                        }
                        lineCount++;
                    }
                    if (lineCount >= MAX_OUTPUT_LINES) {
                        synchronized (errorOutput) {
                            errorOutput.append("\n... (вывод ошибок обрезан)\n");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка чтения stderr: " + e.getMessage());
                }
            });
            
            stdoutThread.start();
            stderrThread.start();
            
            // Ждем завершения команды с таймаутом
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            // Даем потокам время дочитать оставшийся вывод
            stdoutThread.join(1000);
            stderrThread.join(1000);
            
            if (!finished) {
                process.destroyForcibly();
                
                StringBuilder result = new StringBuilder();
                result.append("COMMAND TIMEOUT\n");
                result.append("Команда не завершилась за ").append(timeoutSeconds).append(" секунд\n");
                result.append("Показан вывод, который успел накопиться:\n\n");
                
                synchronized (output) {
                    if (output.length() > 0) {
                        result.append("=== STDOUT (частичный) ===\n");
                        result.append(output);
                    } else {
                        result.append("(вывод отсутствует)\n");
                    }
                }
                
                synchronized (errorOutput) {
                    if (errorOutput.length() > 0) {
                        result.append("\n=== STDERR (частичный) ===\n");
                        result.append(errorOutput);
                    }
                }
                
                result.append("\nПроцесс был принудительно остановлен\n");
                System.err.println("Таймаут команды");
                return result.toString();
            }
            
            // Команда завершилась успешно
            StringBuilder result = new StringBuilder();
            
            synchronized (output) {
                if (output.length() > 0) {
                    result.append("=== STDOUT ===\n");
                    result.append(output);
                }
            }
            
            synchronized (errorOutput) {
                if (errorOutput.length() > 0) {
                    result.append("\n=== STDERR ===\n");
                    result.append(errorOutput);
                }
            }
            
            int exitCode = process.exitValue();
            result.append("\n=== EXIT CODE ===\n");
            result.append("Exit code: ").append(exitCode);
            
            if (result.length() > MAX_OUTPUT_CHARS) {
                String truncated = result.substring(0, MAX_OUTPUT_CHARS);
                result = new StringBuilder(truncated);
                result.append("\n... (вывод обрезан, лимит ").append(MAX_OUTPUT_CHARS).append(" символов)\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            StringBuilder result = new StringBuilder();
            result.append("EXCEPTION\n");
            result.append("Exception: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
            
            Process process = processRef.get();
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                result.append("Процесс был принудительно завершен после ошибки\n");
            }
            
            return result.toString();
        }
    }
}