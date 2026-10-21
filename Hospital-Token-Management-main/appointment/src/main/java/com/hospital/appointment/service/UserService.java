package com.hospital.appointment.service;

import com.hospital.appointment.dto.UserRegisterDto;
import com.hospital.appointment.entity.Patient;
import com.hospital.appointment.entity.Role;
import com.hospital.appointment.entity.User;
import com.hospital.appointment.repository.PatientRepository;
import com.hospital.appointment.repository.RoleRepository;
import com.hospital.appointment.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;

/**
 * Service class to manage user-related operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PatientRepository patientRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new patient.
     * 
     * @param registerDto form data containing registration details
     * @return the saved Patient profile
     * @throws IllegalArgumentException if the email is already in use
     */
    @Transactional
    public Patient registerPatient(UserRegisterDto registerDto) {
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        // 1. Create and configure the user credential entity
        User user = new User();
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setEnabled(true);

        // Fetch or initialize the patient role
        Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_PATIENT")));

        Set<Role> roles = new HashSet<>();
        roles.add(patientRole);
        user.setRoles(roles);

        // Save User (managed cascade or manual save)
        User savedUser = userRepository.save(user);

        // 2. Create and configure the patient profile entity
        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setFirstName(registerDto.getFirstName());
        patient.setLastName(registerDto.getLastName());
        patient.setPhone(registerDto.getPhone());
        patient.setDateOfBirth(registerDto.getDateOfBirth());

        return patientRepository.save(patient);
    }
}
