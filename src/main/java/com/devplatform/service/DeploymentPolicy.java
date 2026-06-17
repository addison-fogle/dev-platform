package com.devplatform.service;

import com.devplatform.dto.DeploymentRequest;
import com.devplatform.model.Environment;
import com.devplatform.model.Service;

@org.springframework.stereotype.Service
public class DeploymentPolicy {

    public void validateCreate(DeploymentRequest request, Service service, Environment environment) {
        if (!isProduction(environment)) {
            return;
        }

        if (isBlank(service.getOwner())) {
            throw new IllegalArgumentException("Production deployments require a service owner.");
        }
        if (isBlank(request.deployedBy())) {
            throw new IllegalArgumentException("Production deployments require deployedBy.");
        }
        if (usesLatestTag(request.imageTag())) {
            throw new IllegalArgumentException("Production deployments must use an immutable image tag.");
        }
        if (isBlank(environment.getNamespace())) {
            throw new IllegalArgumentException("Production deployments require an environment namespace.");
        }
    }

    private boolean isProduction(Environment environment) {
        String name = environment.getName();
        return name != null && (name.equalsIgnoreCase("prod") || name.equalsIgnoreCase("production"));
    }

    private boolean usesLatestTag(String imageTag) {
        String normalized = imageTag == null ? "" : imageTag.trim().toLowerCase();
        return normalized.equals("latest") || normalized.endsWith(":latest");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
