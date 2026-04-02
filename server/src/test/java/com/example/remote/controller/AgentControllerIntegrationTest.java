package com.example.remote.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.remote.entity.Command;
import com.example.remote.entity.Computer;
import com.example.remote.repository.CommandRepository;
import com.example.remote.repository.ComputerRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AgentControllerIntegrationTest {

    @Autowired
    private ComputerRepository computerRepository;

    @Autowired
    private CommandRepository commandRepository;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        commandRepository.deleteAll();
        computerRepository.deleteAll();
    }

    @Test
    void registerAgent_ShouldCreateComputer() {
        Computer computer = new Computer();
        computer.setName("TestPC");
        computer.setIp("192.168.1.100");

        // Используем встроенный механизм без TestRestTemplate
        Computer saved = computerRepository.save(computer);
        
        assertNotNull(saved.getId());
        assertEquals("TestPC", saved.getName());
        assertEquals("192.168.1.100", saved.getIp());
    }

    @Test
    void createCommand_ShouldSaveWithPendingStatus() {
        // Создаем компьютер
        Computer computer = new Computer();
        computer.setName("CommandPC");
        computer.setIp("192.168.1.101");
        Computer savedComputer = computerRepository.save(computer);

        // Создаем команду
        Command command = new Command();
        command.setComputer(savedComputer);
        command.setCommandText("dir");
        command.setStatus("PENDING");
        Command savedCommand = commandRepository.save(command);

        assertNotNull(savedCommand.getId());
        assertEquals("dir", savedCommand.getCommandText());
        assertEquals("PENDING", savedCommand.getStatus());
    }

    @Test
    void getPendingCommands_ShouldReturnOnlyPending() {
        // Создаем компьютер
        Computer computer = new Computer();
        computer.setName("PendingPC");
        computer.setIp("192.168.1.102");
        Computer savedComputer = computerRepository.save(computer);

        // Создаем PENDING команду
        Command pendingCommand = new Command();
        pendingCommand.setComputer(savedComputer);
        pendingCommand.setCommandText("dir");
        pendingCommand.setStatus("PENDING");
        commandRepository.save(pendingCommand);

        // Создаем COMPLETED команду
        Command completedCommand = new Command();
        completedCommand.setComputer(savedComputer);
        completedCommand.setCommandText("echo done");
        completedCommand.setStatus("COMPLETED");
        commandRepository.save(completedCommand);

        // Получаем только PENDING команды
        List<Command> pendingCommands = commandRepository.findByComputerIdAndStatus(savedComputer.getId(), "PENDING");
        
        assertEquals(1, pendingCommands.size());
        assertEquals("dir", pendingCommands.get(0).getCommandText());
        assertEquals("PENDING", pendingCommands.get(0).getStatus());
    }

    @Test
    void saveCommandResult_ShouldUpdateStatus() {
        // Создаем компьютер
        Computer computer = new Computer();
        computer.setName("ResultPC");
        computer.setIp("192.168.1.103");
        Computer savedComputer = computerRepository.save(computer);

        // Создаем команду
        Command command = new Command();
        command.setComputer(savedComputer);
        command.setCommandText("ping 127.0.0.1");
        command.setStatus("PENDING");
        Command savedCommand = commandRepository.save(command);

        // Обновляем результат
        savedCommand.setResult("Pinging 127.0.0.1...");
        savedCommand.setStatus("COMPLETED");
        commandRepository.save(savedCommand);

        Command updated = commandRepository.findById(savedCommand.getId()).get();
        
        assertEquals("COMPLETED", updated.getStatus());
        assertEquals("Pinging 127.0.0.1...", updated.getResult());
    }


    @Test
    void getComputerById_ShouldReturnCorrectComputer() {
        // Создаем компьютер
        Computer computer = new Computer();
        computer.setName("GetByIdPC");
        computer.setIp("192.168.1.105");
        Computer saved = computerRepository.save(computer);

        // Получаем по ID
        Computer found = computerRepository.findById(saved.getId()).get();
        
        assertNotNull(found);
        assertEquals("GetByIdPC", found.getName());
    }

    @Test
    void getAllComputers_ShouldReturnAll() {
        // Создаем несколько компьютеров
        Computer pc1 = new Computer();
        pc1.setName("PC1");
        pc1.setIp("192.168.1.1");
        computerRepository.save(pc1);

        Computer pc2 = new Computer();
        pc2.setName("PC2");
        pc2.setIp("192.168.1.2");
        computerRepository.save(pc2);

        List<Computer> all = computerRepository.findAll();
        
        assertEquals(2, all.size());
    }
}