package com.example.remote.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.remote.entity.Computer;

@Repository
public interface ComputerRepository extends JpaRepository<Computer, Long> {
    Optional<Computer> findByName(String name);
}