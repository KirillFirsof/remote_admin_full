package com.example.remote.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.remote.entity.Computer;
import com.example.remote.repository.ComputerRepository;

@ExtendWith(MockitoExtension.class)
class ComputerServiceTest {

    @Mock
    private ComputerRepository computerRepository;

    @InjectMocks
    private ComputerService computerService;

    private Computer testComputer;

    @BeforeEach
    void setUp() {
        testComputer = new Computer();
        testComputer.setId(1L);
        testComputer.setName("TestPC");
        testComputer.setIp("192.168.1.100");
        testComputer.setCpuLoad(45.2);
        testComputer.setFreeRamMb(2048);
        testComputer.setLastSeen(LocalDateTime.now());
    }

    @Test
    void registerComputer_ShouldSaveComputer() {
        when(computerRepository.save(any(Computer.class))).thenReturn(testComputer);
        
        Computer result = computerService.registerComputer(testComputer);
        
        assertNotNull(result);
        assertEquals("TestPC", result.getName());
        assertNotNull(result.getLastSeen());
        verify(computerRepository, times(1)).save(testComputer);
    }

    @Test
    void updateHeartbeat_ShouldUpdateComputerMetrics() {
        when(computerRepository.findById(1L)).thenReturn(Optional.of(testComputer));
        when(computerRepository.save(any(Computer.class))).thenReturn(testComputer);
        
        Computer result = computerService.updateHeartbeat(1L, 75.5, 1024);
        
        assertEquals(75.5, result.getCpuLoad());
        assertEquals(1024, result.getFreeRamMb());
        assertNotNull(result.getLastSeen());
        verify(computerRepository, times(1)).save(testComputer);
    }

    @Test
    void updateHeartbeat_ShouldThrowException_WhenComputerNotFound() {
        when(computerRepository.findById(99L)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            computerService.updateHeartbeat(99L, 50.0, 2048);
        });
        
        assertTrue(exception.getMessage().contains("Computer not found"));
    }

    @Test
    void getAllComputers_ShouldReturnListOfComputers() {
        List<Computer> computers = Arrays.asList(testComputer);
        when(computerRepository.findAll()).thenReturn(computers);
        
        List<Computer> result = computerService.getAllComputers();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("TestPC", result.get(0).getName());
        verify(computerRepository, times(1)).findAll();
    }

    @Test
    void getComputerById_ShouldReturnComputer() {
        when(computerRepository.findById(1L)).thenReturn(Optional.of(testComputer));
        
        Computer result = computerService.getComputerById(1L);
        
        assertNotNull(result);
        assertEquals("TestPC", result.getName());
    }

    @Test
    void getComputerById_ShouldThrowException_WhenNotFound() {
        when(computerRepository.findById(99L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> {
            computerService.getComputerById(99L);
        });
    }
}