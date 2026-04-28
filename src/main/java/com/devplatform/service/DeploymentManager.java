package com.devplatform.service;

import com.devplatform.dto.DeploymentRequest;
import com.devplatform.exceptions.NotFoundException;
import com.devplatform.messaging.DeploymentEventPublisher;
import com.devplatform.model.Deployment;
import com.devplatform.model.DeploymentStatus;
import com.devplatform.model.Environment;
import com.devplatform.model.History;
import com.devplatform.model.Service;
import com.devplatform.repository.DeploymentRepository;
import com.devplatform.repository.EnvironmentRepository;
import com.devplatform.repository.ServiceRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class DeploymentManager {

    private final DeploymentRepository deploymentRepository;
    private final ServiceRepository serviceRepository;
    private final EnvironmentRepository environmentRepository;
    private final HistoryManager historyManager;
    private final MeterRegistry meterRegistry;
    private final DeploymentEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<Deployment> getAll() {
        return deploymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Deployment getById(Long id) {
        return deploymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Deployment not found: " + id));
    }

    @Transactional
    public Deployment create(DeploymentRequest request, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Deployment> existing = deploymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) return existing.get();
        }

        Service service = serviceRepository.findByName(request.serviceName())
                .orElseThrow(() -> new NotFoundException("Service not found: " + request.serviceName()));
        Environment environment = environmentRepository.findByName(request.environment())
                .orElseThrow(() -> new NotFoundException("Environment not found: " + request.environment()));

        Deployment deployment = new Deployment();
        deployment.setService(service);
        deployment.setEnvironment(environment);
        deployment.setImageTag(request.imageTag());
        deployment.setDeployedBy(request.deployedBy());
        deployment.setStatus(DeploymentStatus.PENDING);
        deployment.setCurrent(false);
        deployment.setIdempotencyKey(idempotencyKey);

        Deployment saved;
        try {
            saved = deploymentRepository.saveAndFlush(deployment);
        } catch (DataIntegrityViolationException ex) {
            if (idempotencyKey != null) {
                return deploymentRepository.findByIdempotencyKey(idempotencyKey)
                        .orElseThrow(() -> ex);
            }
            throw ex;
        }
        eventPublisher.publishCreated(saved);
        return saved;
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

        Counter.builder("deployments.status.transitions")
                .tag("from", previous.name())
                .tag("to", status.name())
                .register(meterRegistry)
                .increment();

        eventPublisher.publishStatusChanged(saved, previous);
        return saved;
    }

    public List<History> getHistory(Long deploymentId) {
        return historyManager.getByDeploymentId(deploymentId);
    }

    @Transactional
    public void delete(Long id) {
        Deployment deployment = getById(id);
        historyManager.deleteByDeploymentId(deployment.getId());
        deploymentRepository.delete(deployment);
    }

    @Transactional(readOnly = true)
    public List<Deployment> getCurrentByEnvironment(String environmentName) {
        Environment environment = environmentRepository.findByName(environmentName)
                .orElseThrow(() -> new NotFoundException("Environment not found: " + environmentName));
        return deploymentRepository.findByEnvironmentAndCurrentTrue(environment);
    }
}