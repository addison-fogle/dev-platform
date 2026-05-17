package com.devplatform.dto;

import jakarta.validation.constraints.NotBlank;

public record ServiceCreateRequest(
        @NotBlank String name,
        String owner,
        String imageRegistry
) {}
