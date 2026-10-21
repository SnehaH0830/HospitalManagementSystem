package com.hospital.appointment.repository;

import com.hospital.appointment.entity.DailyQueue;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository interface for DailyQueue database operations, including pessimistic locks.
 */
public interface DailyQueueRepository extends JpaRepository<DailyQueue, Long> {

    /**
     * Find a daily queue by doctor and date without lock.
     * 
     * @param doctorId doctor id
     * @param queueDate queue date
     * @return Optional containing DailyQueue if found
     */
    Optional<DailyQueue> findByDoctorIdAndQueueDate(Long doctorId, LocalDate queueDate);

    /**
     * Find a daily queue by doctor and date, applying a write lock (SELECT FOR UPDATE).
     * This blocks concurrent threads from reading/updating this queue until the current transaction commits.
     * 
     * @param doctorId doctor id
     * @param queueDate queue date
     * @return Optional containing the locked DailyQueue if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM DailyQueue q WHERE q.doctor.id = :doctorId AND q.queueDate = :queueDate")
    Optional<DailyQueue> findByDoctorIdAndQueueDateWithLock(@Param("doctorId") Long doctorId, @Param("queueDate") LocalDate queueDate);
}
