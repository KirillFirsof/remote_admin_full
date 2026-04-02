package com.example.remote.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.example.remote.service.CommandService;
import com.example.remote.service.ComputerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Агенты", description = "Управление агентами и командами")
@CrossOrigin(origins = "http://localhost:5173")
public class AgentController {

    @Autowired
    private ComputerService computerService;

    @Autowired
    private CommandService commandService;

    @PostMapping("/agent/register")
    @Operation(summary = "Регистрация нового агента")
    public String registerAgent(@RequestBody Computer computer) {
        Computer saved = computerService.registerComputer(computer);
        return "Agent registered with id: " + saved.getId();
    }

    @PostMapping("/heartbeat")
    @Operation(summary = "Получение метрик от агента")
    public String heartbeat(@RequestBody Map<String, Object> request) {
        Long agentId = ((Number) request.get("agentId")).longValue();
        Double cpuLoad = ((Number) request.get("cpuLoad")).doubleValue();
        Integer freeRamMb = ((Number) request.get("freeRamMb")).intValue();
        
        computerService.updateHeartbeat(agentId, cpuLoad, freeRamMb);
        return "OK";
    }

    @GetMapping("/computers")
    @Operation(summary = "Получить список всех компьютеров")
    public List<Computer> getAllComputers() {
        return computerService.getAllComputers();
    }

    @GetMapping("/computers/{id}")
    @Operation(summary = "Получить компьютер по ID")
    public Computer getComputerById(@PathVariable Long id) {
        return computerService.getComputerById(id);
    }

    @PostMapping("/computers/{id}/command")
    @Operation(summary = "Отправить команду на компьютер")
    public Command sendCommand(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String commandText = body.get("commandText");
        return commandService.createCommand(id, commandText);
    }

    @GetMapping("/commands/pending")
    @Operation(summary = "Получить ожидающие команды для агента")
    public List<Command> getPendingCommands(@RequestParam Long agentId) {
        return commandService.getPendingCommands(agentId);
    }

    @PostMapping("/commands/{id}/result")
    @Operation(summary = "Сохранить результат выполнения команды")
    public void saveCommandResult(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String result = body.get("result");
        commandService.saveCommandResult(id, result);
    }

    @GetMapping("/commands")
    @Operation(summary = "Получить историю команд для компьютера")
    public List<Command> getCommandHistory(@RequestParam Long computerId) {
        return commandService.getCommandHistory(computerId);
    }

    @GetMapping("/test")
    @Operation(summary = "Проверка работоспособности сервера")
    public String test() {
        return "Server works!";
    }
}