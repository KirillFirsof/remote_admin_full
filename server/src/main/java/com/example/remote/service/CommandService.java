package com.example.remote.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.remote.entity.Command;
import com.example.remote.entity.Computer;
import com.example.remote.repository.CommandRepository;
import com.example.remote.repository.ComputerRepository;

@Service
public class CommandService {

    @Autowired
    private CommandRepository commandRepository;

    @Autowired
    private ComputerRepository computerRepository;

    public Command createCommand(Long computerId, String commandText) {
        Computer computer = computerRepository.findById(computerId)
                .orElseThrow(() -> new RuntimeException("Computer not found with id: " + computerId));
        
        Command command = new Command();
        command.setComputer(computer);
        command.setCommandText(commandText);
        command.setStatus("PENDING");
        command.setCreatedAt(LocalDateTime.now());
        
        return commandRepository.save(command);
    }

    public List<Command> getPendingCommands(Long agentId) {
        return commandRepository.findByComputerIdAndStatus(agentId, "PENDING");
    }

    public Command saveCommandResult(Long commandId, String result) {
        Command command = commandRepository.findById(commandId)
                .orElseThrow(() -> new RuntimeException("Command not found with id: " + commandId));
        command.setResult(result);
        command.setStatus("COMPLETED");
        return commandRepository.save(command);
    }

    public List<Command> getCommandHistory(Long computerId) {
        return commandRepository.findByComputerIdOrderByCreatedAtDesc(computerId);
    }
}