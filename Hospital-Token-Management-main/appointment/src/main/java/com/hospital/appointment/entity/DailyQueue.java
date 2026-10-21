package com.hospital.appointment.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entity tracking the state of a doctor's active queue on a specific date.
 */
@Entity
@Table(
    name = "daily_queues",
    uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "queue_date"})
)
public class DailyQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;

    @Column(name = "current_token", nullable = false)
    private int currentToken = 0; // last called token number

    @Column(name = "total_tokens", nullable = false)
    private int totalTokens = 0; // highest allocated token number (prevents duplicate tokens)

    public DailyQueue() {
    }

    public DailyQueue(Doctor doctor, LocalDate queueDate) {
        this.doctor = doctor;
        this.queueDate = queueDate;
        this.currentToken = 0;
        this.totalTokens = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDate getQueueDate() {
        return queueDate;
    }

    public void setQueueDate(LocalDate queueDate) {
        this.queueDate = queueDate;
    }

    public int getCurrentToken() {
        return currentToken;
    }

    public void setCurrentToken(int currentToken) {
        this.currentToken = currentToken;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }

    @Override
    public String toString() {
        return "DailyQueue{" +
                "id=" + id +
                ", queueDate=" + queueDate +
                ", currentToken=" + currentToken +
                ", totalTokens=" + totalTokens +
                '}';
    }
}
