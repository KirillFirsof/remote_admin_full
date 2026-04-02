package com.example.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @TempDir
    Path tempDir;
    
    private Config config;
    private File configFile;

    @BeforeEach
    void setUp() {
        // Используем временную директорию для тестов
        System.setProperty("user.dir", tempDir.toString());
        config = new Config();
        configFile = new File(tempDir.toString(), "agent.properties");
    }

    @Test
    void setAgentId_ShouldSaveAndRetrieve() {
        config.setAgentId("test-agent-123");
        
        assertTrue(config.hasAgentId());
        assertEquals("test-agent-123", config.getAgentId());
    }

    @Test
    void setAgentId_ShouldPersistToFile() {
        config.setAgentId("persistent-id");
        
        // Создаем новый экземпляр Config для проверки загрузки
        Config newConfig = new Config();
        assertTrue(newConfig.hasAgentId());
        assertEquals("persistent-id", newConfig.getAgentId());
    }

    @Test
    void overwriteAgentId_ShouldUpdateValue() {
        config.setAgentId("first-id");
        assertEquals("first-id", config.getAgentId());
        
        config.setAgentId("second-id");
        assertEquals("second-id", config.getAgentId());
    }


}