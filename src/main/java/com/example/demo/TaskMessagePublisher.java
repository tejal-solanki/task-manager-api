package com.example.demo;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TaskMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public TaskMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTaskCreated(String message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                message
        );
        System.out.println("Published to RabbitMQ: " + message);
    }
    public void publishUrgentTask(String message) {
    rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE,
            RabbitMQConfig.DUE_ROUTING_KEY,  // reuse the due notification queue for urgents
            message
    );
    System.out.println("[AI] Published urgent task to RabbitMQ: " + message);
}
}