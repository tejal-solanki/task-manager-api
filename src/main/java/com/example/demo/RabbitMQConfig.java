package com.example.demo;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "task.queue";
    public static final String EXCHANGE = "task.exchange";
    public static final String ROUTING_KEY = "task.created";

    // NEW
    public static final String DUE_QUEUE = "due.notification.queue";
    public static final String DUE_ROUTING_KEY = "task.due";

    @Bean
public RabbitAdmin rabbitAdmin(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
}

    @Bean
    public Queue taskQueue() {
        return new Queue(QUEUE, true);
    }

    // NEW
    @Bean
    public Queue dueNotificationQueue() {
        return new Queue(DUE_QUEUE, true);
    }

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue taskQueue, DirectExchange taskExchange) {
        return BindingBuilder
                .bind(taskQueue)
                .to(taskExchange)
                .with(ROUTING_KEY);
    }

    // NEW
    @Bean
    public Binding dueBinding(Queue dueNotificationQueue, DirectExchange taskExchange) {
        return BindingBuilder
                .bind(dueNotificationQueue)
                .to(taskExchange)
                .with(DUE_ROUTING_KEY);
    }
}