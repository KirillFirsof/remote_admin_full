package com.example.remote.service;

import com.example.remote.entity.Command;
import com.example.remote.entity.Computer;
import com.example.remote.repository.CommandRepository;
import com.example.remote.repository.ComputerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandServiceTest {

    @Mock
    private CommandRepository commandRepository;

    @Mock
    private ComputerRepository computerRepository;

    @InjectMocks
    private CommandService commandService;

    private Computer testComputer;
    private Command testCommand;

    @BeforeEach
    void setUp() {
        testComputer = new Computer();
        testComputer.setId(1L);
        testComputer.setName("TestPC");

        testCommand = new Command();
        testCommand.setId(1L);
        testCommand.setComputer(testComputer);
        testCommand.setCommandText("dir");
        testCommand.setStatus("PENDING");
        testCommand.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createCommand_ShouldSaveCommandWithPendingStatus() {
        when(computerRepository.findById(1L)).thenReturn(Optional.of(testComputer));
        when(commandRepository.save(any(Command.class))).thenReturn(testCommand);
        
        Command result = commandService.createCommand(1L, "dir");
        
        assertNotNull(result);
        assertEquals("dir", result.getCommandText());
        assertEquals("PENDING", result.getStatus());
        assertNotNull(result.getCreatedAt());
        verify(commandRepository, times(1)).save(any(Command.class));
    }

    @Test
    void createCommand_ShouldThrowException_WhenComputerNotFound() {
        when(computerRepository.findById(99L)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            commandService.createCommand(99L, "dir");
        });
        
        assertTrue(exception.getMessage().contains("Computer not found"));
    }

    @Test
    void getPendingCommands_ShouldReturnOnlyPendingCommands() {
        List<Command> commands = Arrays.asList(testCommand);
        when(commandRepository.findByComputerIdAndStatus(1L, "PENDING")).thenReturn(commands);
        
        List<Command> result = commandService.getPendingCommands(1L);
        
        assertFalse(result.isEmpty());
        assertEquals("PENDING", result.get(0).getStatus());
        verify(commandRepository, times(1)).findByComputerIdAndStatus(1L, "PENDING");
    }

    @Test
    void getPendingCommands_ShouldReturnEmptyList_WhenNoPendingCommands() {
        when(commandRepository.findByComputerIdAndStatus(1L, "PENDING")).thenReturn(Arrays.asList());
        
        List<Command> result = commandService.getPendingCommands(1L);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void saveCommandResult_ShouldUpdateStatusToCompleted() {
        when(commandRepository.findById(1L)).thenReturn(Optional.of(testCommand));
        when(commandRepository.save(any(Command.class))).thenReturn(testCommand);
        
        Command result = commandService.saveCommandResult(1L, "Command output");
        
        assertEquals("COMPLETED", result.getStatus());
        assertEquals("Command output", result.getResult());
        verify(commandRepository, times(1)).save(testCommand);
    }

    @Test
    void saveCommandResult_ShouldThrowException_WhenCommandNotFound() {
        when(commandRepository.findById(99L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> {
            commandService.saveCommandResult(99L, "output");
        });
    }

    @Test
    void getCommandHistory_ShouldReturnCommandsOrderedByDate() {
        List<Command> commands = Arrays.asList(testCommand);
        when(commandRepository.findByComputerIdOrderByCreatedAtDesc(1L)).thenReturn(commands);
        
        List<Command> result = commandService.getCommandHistory(1L);
        
        assertFalse(result.isEmpty());
        verify(commandRepository, times(1)).findByComputerIdOrderByCreatedAtDesc(1L);
    }
}
