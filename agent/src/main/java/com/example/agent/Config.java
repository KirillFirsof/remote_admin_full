package com.example.agent;

import java.io.*;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "agent.properties";
    private Properties props;
    
    public Config() {
        props = new Properties();
        load();
    }
    
    private void load() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Не удалось загрузить конфиг: " + e.getMessage());
            }
        }
    }
    
    public void save() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Agent Configuration");
        } catch (IOException e) {
            System.err.println("Не удалось сохранить конфиг: " + e.getMessage());
        }
    }
    
    public String getAgentId() {
        return props.getProperty("agent.id");
    }
    
    public void setAgentId(String agentId) {
        props.setProperty("agent.id", agentId);
        save();
    }
    
    public boolean hasAgentId() {
        return props.containsKey("agent.id");
    }
}