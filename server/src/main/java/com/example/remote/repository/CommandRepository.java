package com.example.remote.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.remote.entity.Command;

@Repository
public interface CommandRepository extends JpaRepository<Command, Long> {
    List<Command> findByComputerIdAndStatus(Long computerId, String status);
    List<Command> findByStatus(String status);
    List<Command> findByComputerIdOrderByCreatedAtDesc(Long computerId);
}
