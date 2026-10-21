package com.hospital.appointment.repository;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Appointment database operations.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Find all appointments booked by a patient.
     * 
     * @param patientId patient id
     * @return list of appointments
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Find all appointments for a doctor on a specific date.
     * 
     * @param doctorId doctor id
     * @param date appointment date
     * @return list of appointments
     */
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);

    /**
     * Find all appointments for a doctor on a specific date matching specified statuses.
     * 
     * @param doctorId doctor id
     * @param date appointment date
     * @param statuses list of active/waiting statuses
     * @return list of matching appointments
     */
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusIn(Long doctorId, LocalDate date, List<AppointmentStatus> statuses);

    /**
     * Find a unique appointment matching doctor, date, and token number.
     * 
     * @param doctorId doctor id
     * @param date appointment date
     * @param tokenNumber token number
     * @return Optional containing the Appointment if found
     */
    Optional<Appointment> findByDoctorIdAndAppointmentDateAndTokenNumber(Long doctorId, LocalDate date, int tokenNumber);

    /**
     * Count total appointments matching a specific status.
     * 
     * @param status appointment status
     * @return count of appointments
     */
    long countByStatus(AppointmentStatus status);
}
