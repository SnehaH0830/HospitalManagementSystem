package com.hospital.appointment.repository;

import com.hospital.appointment.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for NotificationLog database operations.
 */
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}
