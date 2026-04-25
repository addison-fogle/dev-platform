package com.devplatform.controller;

import com.devplatform.model.Environment;
import com.devplatform.service.EnvironmentManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/environments")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentManager environmentManager;

    @GetMapping
    public List<Environment> getAll() {
        return environmentManager.getAll();
    }

    @GetMapping("/{id}")
    public Environment getById(@PathVariable Long id) {
        return environmentManager.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Environment create(@RequestBody Environment environment) {
        return environmentManager.create(environment);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        environmentManager.delete(id);
    }
}