package com.hospital.appointment.repository;

import com.hospital.appointment.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for Patient database operations.
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Find a patient by the email of their linked User credential.
     * 
     * @param email patient user email
     * @return Optional containing the Patient profile if found
     */
    Optional<Patient> findByUserEmail(String email);
}
