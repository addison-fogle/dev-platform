package com.devplatform.exceptions;

public record ApiError(int status, String message) {}