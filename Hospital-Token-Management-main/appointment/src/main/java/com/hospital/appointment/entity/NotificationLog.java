package com.hospital.appointment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity logging system email notifications sent to users.
 */
@Entity
@Table(name = "notification_logs")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(nullable = false)
    private String type; // e.g. "CONFIRMATION", "TWO_AHEAD", "YOUR_TURN"

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public NotificationLog() {
    }

    public NotificationLog(Appointment appointment, String type, String recipientEmail) {
        this.appointment = appointment;
        this.type = type;
        this.recipientEmail = recipientEmail;
        this.sentAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    @Override
    public String toString() {
        return "NotificationLog{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", recipientEmail='" + recipientEmail + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}
