package com.devplatform.repository;

import com.devplatform.model.Deployment;
import com.devplatform.model.Environment;
import com.devplatform.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

    Optional<Deployment> findByServiceAndEnvironmentAndCurrentTrue(Service service, Environment environment);

    List<Deployment> findByEnvironmentAndCurrentTrue(Environment environment);

    List<Deployment> findByServiceAndEnvironmentOrderByCreatedAtDesc(Service service, Environment environment);

    @Modifying
    @Query("UPDATE Deployment d SET d.current = false WHERE d.service = :service AND d.environment = :environment AND d.current = true")
    void clearCurrent(Service service, Environment environment);
}