package com.devplatform.repository;

import com.devplatform.model.Deployment;
import com.devplatform.model.Environment;
import com.devplatform.model.Service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

    List<Deployment> findByEnvironmentAndCurrentTrue(Environment environment);

    @Modifying
    @Query("UPDATE Deployment d SET d.current = false WHERE d.service = :service AND d.environment = :environment AND d.current = true")
    void clearCurrent(Service service, Environment environment);
}