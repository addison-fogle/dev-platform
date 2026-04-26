package com.devplatform.service;

import com.devplatform.model.Deployment;
import com.devplatform.model.DeploymentStatus;
import com.devplatform.model.History;
import com.devplatform.repository.HistoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryManager {

    private final HistoryRepository historyRepository;

    @Transactional
    public void record(Deployment deployment, DeploymentStatus fromStatus, DeploymentStatus toStatus) {
        History entry = new History();
        entry.setDeployment(deployment);
        entry.setFromStatus(fromStatus);
        entry.setToStatus(toStatus);
        historyRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<History> getByDeploymentId(Long deploymentId) {
        return historyRepository.findByDeploymentId(deploymentId);
    }
}