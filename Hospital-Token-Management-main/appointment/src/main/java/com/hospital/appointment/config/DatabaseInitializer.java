package com.hospital.appointment.config;

import com.hospital.appointment.entity.*;
import com.hospital.appointment.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Component to initialize default database values (roles, default admin, and a sample doctor) on startup.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SpecializationRepository specializationRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository, RoleRepository roleRepository,
                               SpecializationRepository specializationRepository, DoctorRepository doctorRepository,
                               DoctorScheduleRepository doctorScheduleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.specializationRepository = specializationRepository;
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Initialize Roles
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_DOCTOR")));
        Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_PATIENT")));

        // 2. Initialize Default Administrator
        if (!userRepository.existsByEmail("admin@hospital.com")) {
            User adminUser = new User();
            adminUser.setEmail("admin@hospital.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEnabled(true);
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            adminUser.setRoles(roles);
            userRepository.save(adminUser);
            System.out.println(">>> Default Administrator created: admin@hospital.com / admin123");
        }

        // 3. Initialize Sample Specializations
        Specialization cardiology = specializationRepository.findByName("Cardiology")
                .orElseGet(() -> specializationRepository.save(new Specialization("Cardiology", "Heart health and cardiac care")));
        
        specializationRepository.findByName("Pediatrics")
                .orElseGet(() -> specializationRepository.save(new Specialization("Pediatrics", "Medical care for infants, children, and adolescents")));
        
        specializationRepository.findByName("Dermatology")
                .orElseGet(() -> specializationRepository.save(new Specialization("Dermatology", "Skin, hair, and nail treatments")));

        // 4. Initialize Sample Doctor
        if (!userRepository.existsByEmail("doctor@hospital.com")) {
            // Create user account
            User docUser = new User();
            docUser.setEmail("doctor@hospital.com");
            docUser.setPassword(passwordEncoder.encode("doctor123"));
            docUser.setEnabled(true);
            Set<Role> roles = new HashSet<>();
            roles.add(doctorRole);
            docUser.setRoles(roles);
            User savedDocUser = userRepository.save(docUser);

            // Create doctor profile
            Doctor doctor = new Doctor();
            doctor.setUser(savedDocUser);
            doctor.setSpecialization(cardiology);
            doctor.setFirstName("Ravi");
            doctor.setLastName("Prasad");
            doctor.setPhone("9876543210");
            doctor.setRoomNumber("Room 102 (First Floor)");
            doctor.setAvgConsultationTime(8); // 8 minutes
            Doctor savedDoctor = doctorRepository.save(doctor);
            System.out.println(">>> Default Doctor created: doctor@hospital.com / doctor123");

            // Create weekly schedules
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(13, 0);

            doctorScheduleRepository.save(new DoctorSchedule(savedDoctor, DayOfWeek.MONDAY, start, end, false));
            doctorScheduleRepository.save(new DoctorSchedule(savedDoctor, DayOfWeek.TUESDAY, start, end, false));
            doctorScheduleRepository.save(new DoctorSchedule(savedDoctor, DayOfWeek.WEDNESDAY, null, null, true)); // Off
            doctorScheduleRepository.save(new DoctorSchedule(savedDoctor, DayOfWeek.THURSDAY, start, end, false));
            doctorScheduleRepository.save(new DoctorSchedule(savedDoctor, DayOfWeek.FRIDAY, start, end, false));
            doctorScheduleRepository.save(new DoctorSchedule(savedDoctor, DayOfWeek.SATURDAY, null, null, true)); // Off
            doctorScheduleRepository.save(new DoctorSchedule(savedDoctor, DayOfWeek.SUNDAY, null, null, true)); // Off
            System.out.println(">>> Sample schedules created for Dr. Ravi Prasad");
        }
    }
}
