package com.example.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    private static final String SERVER_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Регистрация нового агента
     */
    public String registerAgent(String computerName, String ip) throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("name", computerName);
        requestBody.put("ip", ip);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/agent/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .timeout(Duration.ofSeconds(5))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            // Сервер возвращает строку типа "Agent registered with id: 1"
            String responseBody = response.body();
            // Парсим id из ответа
            String[] parts = responseBody.split(": ");
            return parts[parts.length - 1];
        } else {
            throw new RuntimeException("Ошибка регистрации: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Отправка heartbeat
     */
    public void sendHeartbeat(Long agentId, double cpuLoad, int freeRamMb) throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("agentId", agentId);
        requestBody.put("cpuLoad", cpuLoad);
        requestBody.put("freeRamMb", freeRamMb);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/heartbeat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .timeout(Duration.ofSeconds(5))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Ошибка heartbeat: " + response.statusCode());
        }
    }
}