package com.devplatform.dto;

import jakarta.validation.constraints.NotBlank;

public record DeploymentRequest(
        @NotBlank String serviceName,
        @NotBlank String environment,
        @NotBlank String imageTag,
        String deployedBy
) {}
