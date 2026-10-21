package com.hospital.appointment.service;

import com.hospital.appointment.dto.DoctorDto;
import com.hospital.appointment.entity.*;
import com.hospital.appointment.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service to manage doctors, specializations, and schedules.
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final SpecializationRepository specializationRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository, SpecializationRepository specializationRepository,
                         DoctorScheduleRepository doctorScheduleRepository, UserRepository userRepository,
                         RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.specializationRepository = specializationRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public List<Specialization> getAllSpecializations() {
        return specializationRepository.findAll();
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + id));
    }

    public Specialization getSpecializationById(Long id) {
        return specializationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found with ID: " + id));
    }

    @Transactional
    public Specialization saveSpecialization(Specialization specialization) {
        if (specializationRepository.findByName(specialization.getName()).isPresent()) {
            throw new IllegalArgumentException("Specialization with this name already exists.");
        }
        return specializationRepository.save(specialization);
    }

    @Transactional
    public Doctor createDoctor(DoctorDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        Specialization specialization = specializationRepository.findById(dto.getSpecializationId())
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found"));

        // 1. Create doctor user account
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEnabled(true);

        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_DOCTOR")));

        Set<Role> roles = new HashSet<>();
        roles.add(doctorRole);
        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // 2. Create doctor profile
        Doctor doctor = new Doctor();
        doctor.setUser(savedUser);
        doctor.setSpecialization(specialization);
        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setPhone(dto.getPhone());
        doctor.setRoomNumber(dto.getRoomNumber());
        doctor.setAvgConsultationTime(dto.getAvgConsultationTime());
        Doctor savedDoctor = doctorRepository.save(doctor);

        // 3. Initialize default work schedule (Mon-Fri: 9am-1pm, Sat-Sun: OFF)
        LocalTime defaultStart = LocalTime.of(9, 0);
        LocalTime defaultEnd = LocalTime.of(13, 0);

        for (DayOfWeek day : DayOfWeek.values()) {
            boolean isOff = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
            DoctorSchedule schedule = new DoctorSchedule(
                savedDoctor,
                day,
                isOff ? null : defaultStart,
                isOff ? null : defaultEnd,
                isOff
            );
            doctorScheduleRepository.save(schedule);
        }

        return savedDoctor;
    }

    @Transactional
    public Doctor updateDoctor(DoctorDto dto) {
        Doctor doctor = doctorRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        Specialization specialization = specializationRepository.findById(dto.getSpecializationId())
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found"));

        User user = doctor.getUser();
        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        // Update credentials
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userRepository.save(user);

        // Update profile
        doctor.setSpecialization(specialization);
        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setPhone(dto.getPhone());
        doctor.setRoomNumber(dto.getRoomNumber());
        doctor.setAvgConsultationTime(dto.getAvgConsultationTime());

        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        // Delete associated schedules
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(id);
        doctorScheduleRepository.deleteAll(schedules);

        // Delete profile and user account (cascades where configured)
        User user = doctor.getUser();
        doctorRepository.delete(doctor);
        userRepository.delete(user);
    }

    public List<DoctorSchedule> getSchedulesByDoctorId(Long doctorId) {
        return doctorScheduleRepository.findByDoctorId(doctorId);
    }

    @Transactional
    public void saveDoctorSchedules(Long doctorId, List<DoctorSchedule> updatedSchedules) {
        for (DoctorSchedule updated : updatedSchedules) {
            DoctorSchedule existing = doctorScheduleRepository.findById(updated.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Schedule entry not found"));
            
            existing.setOff(updated.isOff());
            if (updated.isOff()) {
                existing.setStartTime(null);
                existing.setEndTime(null);
            } else {
                existing.setStartTime(updated.getStartTime());
                existing.setEndTime(updated.getEndTime());
            }
            doctorScheduleRepository.save(existing);
        }
    }
}
