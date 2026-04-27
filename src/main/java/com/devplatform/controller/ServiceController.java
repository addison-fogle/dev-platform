package com.devplatform.controller;

import com.devplatform.model.Service;
import com.devplatform.service.ServiceManager;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceManager serviceManager;

    @GetMapping
    public List<Service> getAll() {
        return serviceManager.getAll();
    }

    @GetMapping("/{id}")
    public Service getById(@PathVariable Long id) {
        return serviceManager.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Service create(@RequestBody Service service) {
        return serviceManager.create(service);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        serviceManager.delete(id);
    }
}