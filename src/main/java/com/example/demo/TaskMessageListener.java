package com.example.demo;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class TaskMessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    public TaskMessageListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleTaskCreated(String message) {
        System.out.println("[LISTENER] Task created: " + message);
    }

    @RabbitListener(queues = RabbitMQConfig.DUE_QUEUE)
    public void handleDueNotification(String message) {
        System.out.println("[LISTENER] Due notification: " + message);
        messagingTemplate.convertAndSend("/topic/due-notifications", message);
    }
}