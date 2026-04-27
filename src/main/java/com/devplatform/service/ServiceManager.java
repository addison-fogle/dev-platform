package com.devplatform.service;

import com.devplatform.model.Service;
import com.devplatform.repository.ServiceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceManager {

    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<Service> getAll() {
        return serviceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Service getById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found: " + id));
    }

    @Transactional
    public Service create(Service service) {
        return serviceRepository.save(service);
    }

    @Transactional
    public void delete(Long id) {
        serviceRepository.deleteById(id);
    }
}