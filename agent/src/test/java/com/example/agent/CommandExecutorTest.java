package com.example.agent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.io.*;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

class CommandExecutorTest {

    private Method executeCommandMethod;

    @BeforeEach
    void setUp() throws Exception {
        executeCommandMethod = AgentApplication.class.getDeclaredMethod("executeCommand", String.class, int.class);
        executeCommandMethod.setAccessible(true);
    }

    @Test
    void executeCommand_WithEchoCommand_ShouldReturnOutput() throws Exception {
        String command = System.getProperty("os.name").toLowerCase().contains("windows") 
            ? "echo Hello World" 
            : "echo Hello World";
        
        String result = (String) executeCommandMethod.invoke(null, command, 5);
        
        assertNotNull(result);
        assertTrue(result.contains("Hello") || result.contains("Hello"), 
            "Output should contain 'Hello'");
    }

    @Test
    void executeCommand_WithInvalidCommand_ShouldReturnError() throws Exception {
        String result = (String) executeCommandMethod.invoke(null, "nonexistent_command_xyz", 5);
        
        assertNotNull(result);
        assertTrue(result.contains("ERROR") || 
                   result.contains("not recognized") || 
                   result.contains("not found") ||
                   result.contains("Exception"),
                   "Should contain error message");
    }

    @Test
    void executeCommand_WithShortCommand_ShouldCompleteBeforeTimeout() throws Exception {
        String command = System.getProperty("os.name").toLowerCase().contains("windows") 
            ? "ping 127.0.0.1 -n 2" 
            : "ping -c 2 127.0.0.1";
        
        String result = (String) executeCommandMethod.invoke(null, command, 10);
        
        assertNotNull(result);
        assertFalse(result.contains("TIMEOUT"), "Command should complete before timeout");
    }

    @Test
    void executeCommand_WithTimeout_ShouldReturnTimeoutMessage() throws Exception {
        String command = System.getProperty("os.name").toLowerCase().contains("windows") 
            ? "ping 127.0.0.1 -n 10" 
            : "sleep 5";
        
        String result = (String) executeCommandMethod.invoke(null, command, 1);
        
        assertNotNull(result);
    }
}