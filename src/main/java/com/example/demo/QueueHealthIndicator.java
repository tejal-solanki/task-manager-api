package com.example.demo;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class QueueHealthIndicator implements HealthIndicator {

    private final RabbitAdmin rabbitAdmin;

    public QueueHealthIndicator(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    @Override
    public Health health() {
        try {
            Properties props = rabbitAdmin.getQueueProperties(RabbitMQConfig.DUE_QUEUE);
            if (props == null) {
                return Health.down()
                        .withDetail("queue", RabbitMQConfig.DUE_QUEUE)
                        .withDetail("reason", "Queue not found")
                        .build();
            }
            int depth = (int) props.get("QUEUE_MESSAGE_COUNT");
            if (depth > 1000) {
                return Health.down()
                        .withDetail("queue", RabbitMQConfig.DUE_QUEUE)
                        .withDetail("depth", depth)
                        .withDetail("reason", "Queue depth too high - consumer may be stuck")
                        .build();
            }
            return Health.up()
                    .withDetail("queue", RabbitMQConfig.DUE_QUEUE)
                    .withDetail("depth", depth)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("reason", e.getMessage())
                    .build();
        }
    }
}