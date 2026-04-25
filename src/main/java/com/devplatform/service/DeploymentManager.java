package com.devplatform.service;

import com.devplatform.model.Deployment;
import com.devplatform.repository.DeploymentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeploymentManager {

    private final DeploymentRepository deploymentRepository;

    public List<Deployment> getAll() {
        return deploymentRepository.findAll();
    }

    public Deployment getById(Long id) {
        return deploymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deployment not found: " + id));
    }
}