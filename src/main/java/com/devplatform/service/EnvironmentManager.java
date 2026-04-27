package com.devplatform.service;

import com.devplatform.model.Environment;
import com.devplatform.repository.EnvironmentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvironmentManager {

    private final EnvironmentRepository environmentRepository;

    @Transactional(readOnly = true)
    public List<Environment> getAll() {
        return environmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Environment getById(Long id) {
        return environmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Environment not found: " + id));
    }

    @Transactional
    public Environment create(Environment environment) {
        return environmentRepository.save(environment);
    }

    @Transactional
    public void delete(Long id) {
        environmentRepository.deleteById(id);
    }
}