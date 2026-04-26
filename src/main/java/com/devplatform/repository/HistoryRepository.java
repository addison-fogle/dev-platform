package com.devplatform.repository;

import com.devplatform.model.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {

    List<History> findByDeploymentId(Long deploymentId);
}
