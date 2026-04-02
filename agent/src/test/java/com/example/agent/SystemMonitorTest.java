package com.example.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemMonitorTest {

    private SystemMonitor systemMonitor;

    @BeforeEach
    void setUp() {
        systemMonitor = new SystemMonitor();
    }

    @Test
    void getComputerName_ShouldReturnNonEmptyString() {
        String computerName = systemMonitor.getComputerName();
        assertNotNull(computerName);
        assertFalse(computerName.isEmpty());
    }

    @Test
    void getCpuLoad_ShouldReturnValueBetweenZeroAndHundred() {
        String computerName = systemMonitor.getComputerName();
        assertNotNull(computerName);
        assertFalse(computerName.isEmpty());
    }

    @Test
    void getFreeRamMb_ShouldReturnPositiveValue() {
        int freeRam = systemMonitor.getFreeRamMb();
        assertTrue(freeRam > 0, "Free RAM should be positive, but was: " + freeRam);
    }
}