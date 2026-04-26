package com.devplatform.dto;

public record DeploymentRequest(
        String serviceName,
        String environment,
        String imageTag,
        String deployedBy
) {}
