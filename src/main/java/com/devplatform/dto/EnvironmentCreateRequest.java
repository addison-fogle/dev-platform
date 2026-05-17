package com.devplatform.dto;

import jakarta.validation.constraints.NotBlank;

public record EnvironmentCreateRequest(
        @NotBlank String name,
        String namespace,
        String clusterContext
) {}
