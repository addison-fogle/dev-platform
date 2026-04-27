package com.devplatform.messaging;

import com.devplatform.model.Deployment;
import com.devplatform.model.DeploymentStatus;

import java.time.Instant;

public record DeploymentEvent(
        Long deploymentId,
        String service,
        String environment,
        String imageTag,
        DeploymentStatus fromStatus,
        DeploymentStatus toStatus,
        String deployedBy,
        Instant occurredAt
) {
    public static DeploymentEvent created(Deployment d) {
        return new DeploymentEvent(
                d.getId(), d.getService().getName(), d.getEnvironment().getName(),
                d.getImageTag(), null, d.getStatus(), d.getDeployedBy(), Instant.now());
    }

    public static DeploymentEvent statusChanged(Deployment d, DeploymentStatus from) {
        return new DeploymentEvent(
                d.getId(), d.getService().getName(), d.getEnvironment().getName(),
                d.getImageTag(), from, d.getStatus(), d.getDeployedBy(), Instant.now());
    }
}
