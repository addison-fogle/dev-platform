package com.devplatform.messaging;

import com.devplatform.model.Deployment;
import com.devplatform.model.DeploymentStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeploymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${devplatform.rabbit.exchange}")
    private String exchange;

    @Async("deploymentExecutor")
    public void publishCreated(Deployment deployment) {
        publish("deployment.created", DeploymentEvent.created(deployment));
    }

    @Async("deploymentExecutor")
    public void publishStatusChanged(Deployment deployment, DeploymentStatus from) {
        publish("deployment.status." + deployment.getStatus().name(),
                DeploymentEvent.statusChanged(deployment, from));
    }

    private void publish(String routingKey, DeploymentEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } catch (AmqpException ex) {
            log.warn("Failed to publish deployment event {}: {}", routingKey, ex.getMessage());
        }
    }
}
