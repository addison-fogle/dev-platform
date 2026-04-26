package com.devplatform.service;

import com.devplatform.model.Service;
import com.devplatform.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceManager {

    private final ServiceRepository serviceRepository;

    public List<Service> getAll() {
        return serviceRepository.findAll();
    }

    public Service getById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found: " + id));
    }

    public Service create(Service service) {
        return serviceRepository.save(service);
    }

    public void delete(Long id) {
        serviceRepository.deleteById(id);
    }
}