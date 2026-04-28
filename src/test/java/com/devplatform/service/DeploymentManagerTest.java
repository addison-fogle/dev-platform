package com.devplatform.service;

import com.devplatform.dto.DeploymentRequest;
import com.devplatform.messaging.DeploymentEventPublisher;
import com.devplatform.model.Deployment;
import com.devplatform.model.DeploymentStatus;
import com.devplatform.model.Environment;
import com.devplatform.model.Service;
import com.devplatform.repository.DeploymentRepository;
import com.devplatform.repository.EnvironmentRepository;
import com.devplatform.repository.ServiceRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class DeploymentManagerTest {

    @Mock private DeploymentRepository deploymentRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private EnvironmentRepository environmentRepository;
    @Mock private HistoryManager historyManager;
    @Mock private DeploymentEventPublisher eventPublisher;

    private SimpleMeterRegistry meterRegistry;
    private DeploymentManager manager;

    @BeforeMethod
    void setup() {
        MockitoAnnotations.openMocks(this);
        meterRegistry = new SimpleMeterRegistry();
        manager = new DeploymentManager(
            deploymentRepository, serviceRepository, environmentRepository,
            historyManager, meterRegistry, eventPublisher);
    }

    @Test
    void updateStatus_toSucceeded_clearsCurrentAndSetsFlag() {
        Service service = new Service();
        Environment env = new Environment();
        Deployment d = deployment(1L, service, env, DeploymentStatus.RUNNING);

        when(deploymentRepository.findById(1L)).thenReturn(Optional.of(d));
        when(deploymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Deployment result = manager.updateStatus(1L, DeploymentStatus.SUCCEEDED);

        assertThat(result.isCurrent()).isTrue();
        verify(deploymentRepository).clearCurrent(service, env);
    }

    @Test
    void updateStatus_toFailed_doesNotSetCurrent() {
        Deployment d = deployment(2L, new Service(), new Environment(), DeploymentStatus.RUNNING);
        when(deploymentRepository.findById(2L)).thenReturn(Optional.of(d));
        when(deploymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Deployment result = manager.updateStatus(2L, DeploymentStatus.FAILED);

        assertThat(result.isCurrent()).isFalse();
        verify(deploymentRepository, never()).clearCurrent(any(), any());
    }

    @Test
    void updateStatus_toStopped_doesNotSetCurrent() {
        Deployment d = deployment(3L, new Service(), new Environment(), DeploymentStatus.RUNNING);
        when(deploymentRepository.findById(3L)).thenReturn(Optional.of(d));
        when(deploymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Deployment result = manager.updateStatus(3L, DeploymentStatus.STOPPED);

        assertThat(result.isCurrent()).isFalse();
        verify(deploymentRepository, never()).clearCurrent(any(), any());
    }

    @Test
    void updateStatus_recordsHistoryTransition() {
        Deployment d = deployment(4L, new Service(), new Environment(), DeploymentStatus.PENDING);
        when(deploymentRepository.findById(4L)).thenReturn(Optional.of(d));
        when(deploymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        manager.updateStatus(4L, DeploymentStatus.RUNNING);

        verify(historyManager).record(any(), eq(DeploymentStatus.PENDING), eq(DeploymentStatus.RUNNING));
    }

    @Test
    void updateStatus_incrementsMetricCounter() {
        Deployment d = deployment(5L, new Service(), new Environment(), DeploymentStatus.PENDING);
        when(deploymentRepository.findById(5L)).thenReturn(Optional.of(d));
        when(deploymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        manager.updateStatus(5L, DeploymentStatus.RUNNING);

        assertThat(meterRegistry.counter("deployments.status.transitions",
            "from", "PENDING", "to", "RUNNING").count()).isEqualTo(1.0);
    }

    @Test
    void create_setsStatusToPendingAndCurrentFalse() {
        Service service = new Service();
        service.setName("api");
        Environment env = new Environment();
        env.setName("staging");

        when(serviceRepository.findByName("api")).thenReturn(Optional.of(service));
        when(environmentRepository.findByName("staging")).thenReturn(Optional.of(env));
        when(deploymentRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        Deployment result = manager.create(new DeploymentRequest("api", "staging", "v2.0.0", "alice"), null);

        assertThat(result.getStatus()).isEqualTo(DeploymentStatus.PENDING);
        assertThat(result.isCurrent()).isFalse();
        assertThat(result.getImageTag()).isEqualTo("v2.0.0");
    }

    @Test
    void create_throwsWhenServiceNotFound() {
        when(serviceRepository.findByName("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> manager.create(new DeploymentRequest("missing", "production", "v1.0.0", "alice"), null))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("missing");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(deploymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> manager.getById(99L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("99");
    }

    private Deployment deployment(Long id, Service service, Environment env, DeploymentStatus status) {
        Deployment d = new Deployment();
        d.setId(id);
        d.setService(service);
        d.setEnvironment(env);
        d.setStatus(status);
        d.setCurrent(false);
        return d;
    }
}
