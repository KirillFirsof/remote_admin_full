package com.example.agent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentApplication {
    private static final int HEARTBEAT_INTERVAL_SECONDS = 10;
    
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
}