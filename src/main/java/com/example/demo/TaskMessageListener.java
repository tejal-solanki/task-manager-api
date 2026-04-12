package com.example.demo;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TaskMessageListener {

   @RabbitListener(queues = RabbitMQConfig.QUEUE)
public void handleTaskCreated(String message) {
    System.out.println("[LISTENER] Received from RabbitMQ on thread: " 
        + Thread.currentThread().getName() + " | " + message);
}
}