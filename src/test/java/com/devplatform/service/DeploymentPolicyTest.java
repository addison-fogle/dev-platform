package com.devplatform.service;

import com.devplatform.dto.DeploymentRequest;
import com.devplatform.model.Environment;
import com.devplatform.model.Service;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeploymentPolicyTest {

    private final DeploymentPolicy policy = new DeploymentPolicy();

    @Test
    void productionRejectsLatestTag() {
        Service service = serviceWithOwner();
        Environment environment = productionEnvironment();

        assertThatThrownBy(() -> policy.validateCreate(
                new DeploymentRequest("api", "production", "latest", "alice"), service, environment))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("immutable");
    }

    @Test
    void productionRequiresServiceOwner() {
        Service service = new Service();
        Environment environment = productionEnvironment();

        assertThatThrownBy(() -> policy.validateCreate(
                new DeploymentRequest("api", "production", "v1.0.0", "alice"), service, environment))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("owner");
    }

    @Test
    void productionRequiresDeployerIdentity() {
        Service service = serviceWithOwner();
        Environment environment = productionEnvironment();

        assertThatThrownBy(() -> policy.validateCreate(
                new DeploymentRequest("api", "production", "v1.0.0", ""), service, environment))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("deployedBy");
    }

    private Service serviceWithOwner() {
        Service service = new Service();
        service.setOwner("payments-team");
        return service;
    }

    private Environment productionEnvironment() {
        Environment environment = new Environment();
        environment.setName("production");
        environment.setNamespace("payments-prod");
        return environment;
    }
}
