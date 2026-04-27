package com.devplatform.service;

import com.devplatform.dto.DeploymentRequest;
import com.devplatform.model.Deployment;
import com.devplatform.model.DeploymentStatus;
import com.devplatform.model.Environment;
import com.devplatform.model.History;
import com.devplatform.model.Service;
import com.devplatform.repository.DeploymentRepository;
import com.devplatform.repository.EnvironmentRepository;
import com.devplatform.repository.ServiceRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Getter
@Setter
public class DeploymentManager {

    private final DeploymentRepository deploymentRepository;
    private final ServiceRepository serviceRepository;
    private final EnvironmentRepository environmentRepository;
    private final HistoryManager historyManager;

    public List<Deployment> getAll() {
        return deploymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Deployment getById(Long id) {
        return deploymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deployment not found: " + id));
    }

    @Transactional
    public Deployment create(DeploymentRequest request) {
        Service service = serviceRepository.findByName(request.serviceName())
                .orElseThrow(() -> new RuntimeException("Service not found: " + request.serviceName()));
        Environment environment = environmentRepository.findByName(request.environment())
                .orElseThrow(() -> new RuntimeException("Environment not found: " + request.environment()));

        Deployment deployment = new Deployment();
        deployment.setService(service);
        deployment.setEnvironment(environment);
        deployment.setImageTag(request.imageTag());
        deployment.setDeployedBy(request.deployedBy());
        deployment.setStatus(DeploymentStatus.PENDING);
        deployment.setCurrent(false);

        return deploymentRepository.save(deployment);
    }

    @Transactional
    public Deployment updateStatus(Long id, DeploymentStatus status) {
        Deployment deployment = getById(id);
        DeploymentStatus previous = deployment.getStatus();

        deployment.setStatus(status);

        if (status == DeploymentStatus.SUCCEEDED) {
            deploymentRepository.clearCurrent(deployment.getService(), deployment.getEnvironment());
            deployment.setCurrent(true);
        }

        Deployment saved = deploymentRepository.save(deployment);
        historyManager.record(saved, previous, status);
        return saved;
    }

    public List<History> getHistory(Long deploymentId) {
        return historyManager.getByDeploymentId(deploymentId);
    }

    @Transactional(readOnly = true)
    public List<Deployment> getCurrentByEnvironment(String environmentName) {
        Environment environment = environmentRepository.findByName(environmentName)
                .orElseThrow(() -> new RuntimeException("Environment not found: " + environmentName));
        return deploymentRepository.findByEnvironmentAndCurrentTrue(environment);
    }
}