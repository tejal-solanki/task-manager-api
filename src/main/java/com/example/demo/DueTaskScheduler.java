package com.example.demo;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DueTaskScheduler {

    private final TaskManagerRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;

    public DueTaskScheduler(TaskManagerRepository taskRepository, RabbitTemplate rabbitTemplate) {
        this.taskRepository = taskRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    // Runs every day at 8am
    @Scheduled(cron = "0 0 8 * * *")
    public void checkDueTasks() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<TaskManager> dueTasks = taskRepository.findByDueDate(tomorrow);

        for (TaskManager task : dueTasks) {
            String message = "REMINDER: Task '" + task.getTitle() + "' is due tomorrow!";
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.DUE_ROUTING_KEY,
                    message
            );
            System.out.println("[SCHEDULER] Published due reminder: " + message);
        }
    }
}