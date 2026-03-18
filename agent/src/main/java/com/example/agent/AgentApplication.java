package com.example.agent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.Duration; 

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AgentApplication {
    private static final int HEARTBEAT_INTERVAL_SECONDS = 10;
    private static final String SERVER_URL = "http://localhost:8080/api";
    
    public static void main(String[] args) {
        System.out.println("=== Remote Admin Agent ===");
        
        Config config = new Config();
        SystemMonitor monitor = new SystemMonitor();
        ApiClient apiClient = new ApiClient();
        
        String computerName = monitor.getComputerName();
        String ip = "127.0.0.1"; // В реальности нужно получать реальный IP
        
        Long agentId;
        
        try {
            // Проверяем, есть ли уже agentId
            if (config.hasAgentId()) {
                agentId = Long.parseLong(config.getAgentId());
                System.out.println("Загружен agentId: " + agentId);
            } else {
                // Регистрируем нового агента
                System.out.println("Регистрация нового агента...");
                String idStr = apiClient.registerAgent(computerName, ip);
                agentId = Long.parseLong(idStr);
                config.setAgentId(idStr);
                System.out.println("Зарегистрирован с ID: " + agentId);
            }
            
            // Запускаем периодическую отправку heartbeat
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
                    System.err.println("Ошибка отправки heartbeat: " + e.getMessage());
                }
            }, 0, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
            
            System.out.println("Agent работает. Нажми Ctrl+C для остановки.");
            
            // Держим приложение запущенным
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void checkAndExecuteCommands(Long agentId, ApiClient apiClient) {
        try {
            // 1. Запрос к серверу: есть ли команды для этого агента?
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
                        Long commandId = cmdNode.get("id").asLong();
                        String commandText = cmdNode.get("commandText").asText();

                        System.out.println("Выполняю команду ID " + commandId + ": " + commandText);

                        // 2. Выполняем команду
                        String result = executeCommand(commandText);

                        // 3. Отправляем результат на сервер
                        apiClient.sendCommandResult(commandId, result);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при проверке/выполнении команд: " + e.getMessage());
        }
    }


    private static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);

            // Чтение стандартного вывода
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Чтение потока ошибок
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                output.append("ERROR: ").append(line).append("\n");
            }

            int exitCode = process.waitFor();
            output.append("Exit code: ").append(exitCode);

        } catch (Exception e) {
            output.append("Exception: ").append(e.getMessage());
        }
        return output.toString();
    }

}