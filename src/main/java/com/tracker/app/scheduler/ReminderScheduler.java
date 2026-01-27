package com.tracker.app.scheduler;

import com.tracker.app.entity.Task;
import com.tracker.app.enums.TaskStatus;
import com.tracker.app.repository.TaskRepository;
import com.tracker.app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReminderScheduler {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(fixedRate = 60000)
    public void checkReminders() {

        LocalDateTime now = LocalDateTime.now();

        List<Task> tasks =
                taskRepository.findByReminderSentFalseAndReminderTimeBeforeAndStatusNot(
                        now,
                        TaskStatus.DONE
                );

        for (Task task : tasks) {
            emailService.sendTaskReminder(task);
            task.setReminderSent(true);
            taskRepository.save(task);
        }
        System.out.println("Scheduler running at " + LocalDateTime.now());
        System.out.println("Tasks found: " + tasks.size());


        System.out.println("✅ Reminder job ran. Emails sent: " + tasks.size());

    }
}
