package com.devplatform.service;

import com.devplatform.model.Environment;
import com.devplatform.repository.EnvironmentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvironmentManager {

    private final EnvironmentRepository environmentRepository;

    public List<Environment> getAll() {
        return environmentRepository.findAll();
    }

    public Environment getById(Long id) {
        return environmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Environment not found: " + id));
    }

    public Environment create(Environment environment) {
        return environmentRepository.save(environment);
    }

    public void delete(Long id) {
        environmentRepository.deleteById(id);
    }
}