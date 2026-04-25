package com.devplatform.controller;

import com.devplatform.dto.DeploymentRequest;
import com.devplatform.model.Deployment;
import com.devplatform.service.DeploymentManager;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/deployments")
@RequiredArgsConstructor
public class DeploymentController {

    private final DeploymentManager deploymentManager;

    @GetMapping
    public List<Deployment> getAll() {
        return deploymentManager.getAll();
    }

    @GetMapping("/{id}")
    public Deployment getById(@PathVariable Long id) {
        return deploymentManager.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Deployment create(@RequestBody DeploymentRequest request) {
        return null;
    }
}