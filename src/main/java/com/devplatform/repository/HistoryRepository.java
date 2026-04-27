package com.devplatform.repository;

import com.devplatform.model.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {

    List<History> findByDeploymentId(Long deploymentId);

    @Modifying
    @Query("DELETE FROM History h WHERE h.deployment.id = :deploymentId")
    void deleteByDeploymentId(Long deploymentId);
}
