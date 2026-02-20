package com.example.remote.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.remote.entity.Computer;

public interface ComputerRepository extends JpaRepository<Computer, Long> {
    Optional<Computer> findByName(String name);
}