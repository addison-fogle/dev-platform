package com.devplatform.dto;

public record EnvironmentUpdateRequest(String name, String namespace, String clusterContext) {}
