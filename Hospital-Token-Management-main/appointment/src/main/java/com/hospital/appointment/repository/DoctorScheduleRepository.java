package com.hospital.appointment.repository;

import com.hospital.appointment.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DoctorSchedule database operations.
 */
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    /**
     * Find all schedules for a specific doctor.
     * 
     * @param doctorId doctor id
     * @return list of schedules
     */
    List<DoctorSchedule> findByDoctorId(Long doctorId);

    /**
     * Find schedule of a doctor for a specific day of the week.
     * 
     * @param doctorId doctor id
     * @param dayOfWeek day of week
     * @return Optional containing the schedule if found
     */
    Optional<DoctorSchedule> findByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);
}
