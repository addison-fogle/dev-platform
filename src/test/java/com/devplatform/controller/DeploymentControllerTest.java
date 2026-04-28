package com.devplatform.controller;

import com.devplatform.model.Deployment;
import com.devplatform.model.DeploymentStatus;
import com.devplatform.model.Environment;
import com.devplatform.model.Service;
import com.devplatform.service.DeploymentManager;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
@WebMvcTest(DeploymentController.class)
public class DeploymentControllerTest extends AbstractTestNGSpringContextTests {

    @Autowired MockMvc mockMvc;
    @MockitoBean DeploymentManager deploymentManager;

    @Test
    @WithMockUser
    void getAll_returnsOkWithDeploymentList() throws Exception {
        when(deploymentManager.getAll()).thenReturn(List.of(deployment(DeploymentStatus.RUNNING, false)));

        mockMvc.perform(get("/v1/deployments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("RUNNING"))
            .andExpect(jsonPath("$[0].service.name").value("api"))
            .andExpect(jsonPath("$[0].environment.name").value("production"));
    }

    @Test
    @WithMockUser
    void create_returns201WithPendingDeployment() throws Exception {
        when(deploymentManager.create(any(), any())).thenReturn(deployment(DeploymentStatus.PENDING, false));

        mockMvc.perform(post("/v1/deployments")
                .contentType("application/json")
                .content("""
                    {"serviceName":"api","environment":"production","imageTag":"v1.0.0","deployedBy":"alice"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.imageTag").value("v1.0.0"));
    }

    @Test
    @WithMockUser
    void updateStatus_toSucceeded_returnsCurrentDeployment() throws Exception {
        Deployment succeeded = deployment(DeploymentStatus.SUCCEEDED, true);
        when(deploymentManager.updateStatus(eq(1L), eq(DeploymentStatus.SUCCEEDED))).thenReturn(succeeded);

        mockMvc.perform(patch("/v1/deployments/1/status")
                .contentType("application/json")
                .content("\"SUCCEEDED\""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCEEDED"))
            .andExpect(jsonPath("$.current").value(true));
    }

    @Test
    @WithMockUser
    void getHistory_returnsOk() throws Exception {
        when(deploymentManager.getHistory(1L)).thenReturn(List.of());

        mockMvc.perform(get("/v1/deployments/1/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    void getCurrent_returnsDeploymentsForEnvironment() throws Exception {
        when(deploymentManager.getCurrentByEnvironment("production"))
            .thenReturn(List.of(deployment(DeploymentStatus.SUCCEEDED, true)));

        mockMvc.perform(get("/v1/deployments/current").param("environment", "production"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].current").value(true));
    }

    private Deployment deployment(DeploymentStatus status, boolean current) {
        Service service = new Service();
        service.setName("api");
        Environment env = new Environment();
        env.setName("production");

        Deployment d = new Deployment();
        d.setId(1L);
        d.setService(service);
        d.setEnvironment(env);
        d.setImageTag("v1.0.0");
        d.setStatus(status);
        d.setCurrent(current);
        d.setDeployedBy("alice");
        d.setCreatedAt(Instant.now());
        d.setUpdatedAt(Instant.now());
        return d;
    }
}
