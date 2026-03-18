package com.example.remote.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.remote.entity.Command;
import com.example.remote.entity.Computer;
import com.example.remote.repository.CommandRepository;
import com.example.remote.repository.ComputerRepository;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class CommandController {

    @Autowired
    private CommandRepository commandRepository;

    @Autowired
    private ComputerRepository computerRepository;

    // 1. Создание новой команды
    @PostMapping("/computers/{id}/command")
    @Operation(summary = "Отправить команду на компьютер")
    public ResponseEntity<Command> createCommand(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        Computer computer = computerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Computer not found"));
        
        Command command = new Command();
        command.setComputer(computer);
        command.setCommandText(body.get("commandText"));
        command.setStatus("PENDING");
        command.setCreatedAt(LocalDateTime.now());
        
        Command saved = commandRepository.save(command);
        return ResponseEntity.ok(saved);
    }

    // 2. Получение ожидающих команд для агента
    @GetMapping("/commands/pending")
    @Operation(summary = "Получить ожидающие команды для агента")
    public List<Command> getPendingCommands(@RequestParam Long agentId) {
        return commandRepository.findByComputerIdAndStatus(agentId, "PENDING");
    }

    // 3. Сохранение результата команды
    @PostMapping("/commands/{id}/result")
    @Operation(summary = "Сохранить результат выполнения команды")
    public ResponseEntity<?> saveCommandResult(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        Command command = commandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Command not found"));
        
        command.setResult(body.get("result"));
        command.setStatus("COMPLETED");
        commandRepository.save(command);
        
        return ResponseEntity.ok().build();
    }

    // 4. Получение истории команд для компьютера
    @GetMapping("/commands")
    @Operation(summary = "Получить историю команд для компьютера")
    public List<Command> getCommandsByComputer(@RequestParam Long computerId) {
        return commandRepository.findByComputerIdOrderByCreatedAtDesc(computerId);
    }
}