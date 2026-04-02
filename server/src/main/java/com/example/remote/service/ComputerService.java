package com.example.remote.service;

import com.example.remote.entity.Computer;
import com.example.remote.repository.ComputerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComputerService {

    @Autowired
    private ComputerRepository computerRepository;

    public Computer registerComputer(Computer computer) {
        computer.setLastSeen(LocalDateTime.now());
        return computerRepository.save(computer);
    }

    public Computer updateHeartbeat(Long agentId, Double cpuLoad, Integer freeRamMb) {
        Computer computer = computerRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Computer not found with id: " + agentId));
        computer.setLastSeen(LocalDateTime.now());
        computer.setCpuLoad(cpuLoad);
        computer.setFreeRamMb(freeRamMb);
        return computerRepository.save(computer);
    }

    public List<Computer> getAllComputers() {
        return computerRepository.findAll();
    }

    public Computer getComputerById(Long id) {
        return computerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Computer not found with id: " + id));
    }
}