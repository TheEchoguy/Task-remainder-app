package com.tracker.app.service;

import com.tracker.app.entity.Task;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /* ================= SEND OTP ================= */
    public void sendOTP(String toEmail, String otp) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom("Task Reminder App <your-email@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("Task Reminder App - Email Verification");
            helper.setText(
                    "Your OTP for email verification is: " + otp +
                            "\n\nPlease verify your account within 5 minutes."
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
    public void sendTasksCsv(String toEmail, byte[] csvData) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("Task Reminder App <your-email@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("📄 Your Task List (CSV Export)");

            helper.setText(
                    "Hi 👋,\n\n" +
                            "Attached is your exported task list.\n\n" +
                            "— Task Reminder App"
            );

            helper.addAttachment(
                    "tasks.csv",
                    () -> new java.io.ByteArrayInputStream(csvData)
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send CSV email", e);
        }
    }


    /* ================= TASK REMINDER ================= */
    public void sendTaskReminder(Task task) {

        if (task.getUser() == null || task.getUser().getEmail() == null) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom("Task Reminder App <your-email@gmail.com>");
            helper.setTo(task.getUser().getEmail());
            helper.setSubject("⏰ Task Reminder: " + task.getTitle());

            helper.setText(
                    "Hi " + task.getUser().getName() + ",\n\n" +
                            "This is a reminder for your task:\n\n" +
                            "📌 Task: " + task.getTitle() + "\n" +
                            "📅 Due Date: " + task.getDueDate() + "\n\n" +
                            "Please complete it on time.\n\n" +
                            "— Task Reminder App"
            );

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println(
                    "❌ Reminder failed for task " + task.getId() +
                            " : " + e.getMessage()
            );
        }
    }
}
