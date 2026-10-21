package com.hospital.appointment.service;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.NotificationLog;
import com.hospital.appointment.repository.NotificationLogRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Service to handle dispatching email alerts asynchronously.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationLogRepository notificationLogRepository;

    public EmailService(JavaMailSender mailSender, NotificationLogRepository notificationLogRepository) {
        this.mailSender = mailSender;
        this.notificationLogRepository = notificationLogRepository;
    }

    /**
     * Send booking confirmation.
     */
    @Async
    public void sendBookingConfirmation(Appointment appointment) {
        String recipient = appointment.getPatient().getUser().getEmail();
        String subject = "Appointment Confirmed - Token #" + appointment.getTokenNumber();
        String text = "Dear " + appointment.getPatient().getFirstName() + ",\n\n" +
                "Your appointment has been successfully booked.\n\n" +
                "Appointment Details:\n" +
                "-----------------------------------------\n" +
                "Doctor: Dr. " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName() + "\n" +
                "Specialization: " + appointment.getDoctor().getSpecialization().getName() + "\n" +
                "Date: " + appointment.getAppointmentDate() + "\n" +
                "Your Assigned Token: #" + appointment.getTokenNumber() + "\n" +
                "Room: " + appointment.getDoctor().getRoomNumber() + "\n" +
                "-----------------------------------------\n\n" +
                "You can log in and monitor the live queue progress page on your dashboard.\n\n" +
                "Regards,\nMedQueue Team";

        sendEmail(recipient, subject, text, appointment, "CONFIRMATION");
    }

    /**
     * Send "Two Patients Ahead" alert.
     */
    @Async
    public void sendTwoPatientsAheadAlert(Appointment appointment) {
        String recipient = appointment.getPatient().getUser().getEmail();
        String subject = "Your Appointment is Approaching - Token #" + appointment.getTokenNumber();
        String text = "Dear " + appointment.getPatient().getFirstName() + ",\n\n" +
                "This is a quick notification that you are approaching your turn! Only 2 patients are ahead of you in Dr. " + 
                appointment.getDoctor().getLastName() + "'s queue.\n\n" +
                "Please make sure you are at the consultation room shortly. Your token is #" + appointment.getTokenNumber() + ".\n\n" +
                "Regards,\nMedQueue Team";

        sendEmail(recipient, subject, text, appointment, "TWO_AHEAD");
    }

    /**
     * Send "Your Turn" alert.
     */
    @Async
    public void sendYourTurnAlert(Appointment appointment) {
        String recipient = appointment.getPatient().getUser().getEmail();
        String subject = "Your Turn! Proceed to Consultation Room - Token #" + appointment.getTokenNumber();
        String text = "Dear " + appointment.getPatient().getFirstName() + ",\n\n" +
                "Your token #" + appointment.getTokenNumber() + " has been called!\n" +
                "Please proceed to " + appointment.getDoctor().getRoomNumber() + " immediately for your consultation.\n\n" +
                "Regards,\nMedQueue Team";

        sendEmail(recipient, subject, text, appointment, "YOUR_TURN");
    }

    /**
     * Core SMTP dispatcher method.
     */
    private void sendEmail(String to, String subject, String text, Appointment appointment, String type) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);

            // Log details on success
            NotificationLog log = new NotificationLog(appointment, type, to);
            notificationLogRepository.save(log);
        } catch (Exception e) {
            // Catches connection timeouts/errors so it does not block the transaction rollback
            System.err.println(">>> [MAIL SYSTEM WARNING] Failed to send email to: " + to + " (Type: " + type + ") -> " + e.getMessage());
        }
    }
}
