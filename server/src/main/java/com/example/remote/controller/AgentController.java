package com.example.remote.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.remote.dto.HeartbeatRequest;
import com.example.remote.entity.Command;
import com.example.remote.entity.Computer;
import com.example.remote.repository.ComputerRepository;
import com.example.remote.repository.CommandRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Агенты", description = "Управление агентами и мониторинг")
@CrossOrigin(origins = "http://localhost:5173")
public class AgentController {

    @Autowired
    private ComputerRepository computerRepository;
    private CommandRepository commandRepository;

    @PostMapping("/agent/register")
    @Operation(summary = "Регистрация нового агента", 
               description = "Агент отправляет  имя при первом запуске, сервер возвращает UUID")
    public String registerAgent(@RequestBody Computer computer) {
        computer.setLastSeen(LocalDateTime.now());
        Computer saved = computerRepository.save(computer);
        return "Agent registered with id: " + saved.getId();
    }

    @PostMapping("/heartbeat")
    @Operation(summary = "Получение метрик от агента",
               description = "Агент раз в 10 секунд отправляет данные о загрузке CPU и свободной RAM")
    public String heartbeat(@RequestBody HeartbeatRequest request) {
        Computer computer = computerRepository.findById(request.getAgentId())
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        
        computer.setLastSeen(LocalDateTime.now());
        computer.setCpuLoad(request.getCpuLoad());
        computer.setFreeRamMb(request.getFreeRamMb());
        computerRepository.save(computer);
        
        return "OK";
    }

    @GetMapping("/computers")
    @Operation(summary = "Получить список всех компьютеров",
               description = "Возвращает список всех зарегистрированных агентов с их статусами")
    public List<Computer> getAllComputers() {
        return computerRepository.findAll();
    }

    @GetMapping("/test")
    @Operation(summary = "Проверка работоспособности сервера")
    public String test() {
        return "Сервер работает!";
    }

    @GetMapping("/commands")
    @Operation(summary = "Получить историю команд для компьютера")
    public List<Command> getCommandsByComputer(@RequestParam Long computerId) {
        return commandRepository.findByComputerIdOrderByCreatedAtDesc(computerId);
    }
}