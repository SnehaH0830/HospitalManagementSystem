package com.hospital.appointment.service;

import com.hospital.appointment.entity.*;
import com.hospital.appointment.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Service to manage appointments, token allocations, and live queue position calculations.
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DailyQueueRepository dailyQueueRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final EmailService emailService;

    public AppointmentService(AppointmentRepository appointmentRepository, DailyQueueRepository dailyQueueRepository,
                              DoctorRepository doctorRepository, PatientRepository patientRepository,
                              DoctorScheduleRepository doctorScheduleRepository, EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.dailyQueueRepository = dailyQueueRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.emailService = emailService;
    }

    /**
     * Book a new doctor appointment and allocate a unique queue token number.
     * 
     * Uses pessimistic locking (SELECT FOR UPDATE) on the DailyQueue record to guarantee
     * sequential token allocation (1, 2, 3...) under concurrent requests.
     */
    @Transactional
    public Appointment bookAppointment(Long patientId, Long doctorId, LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot book appointments for past dates.");
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // 1. Verify doctor availability schedule for the day of the week
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        DoctorSchedule schedule = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek)
                .orElseThrow(() -> new IllegalArgumentException("Doctor has no schedule configured for " + dayOfWeek));

        if (schedule.isOff()) {
            throw new IllegalArgumentException("Doctor is not available (Off-day) on " + dayOfWeek);
        }

        // 2. Prevent duplicate active bookings by the same patient for the same doctor on the same day
        List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.BOOKED,
                AppointmentStatus.WAITING,
                AppointmentStatus.CALLED,
                AppointmentStatus.IN_PROGRESS
        );
        List<Appointment> existingList = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusIn(doctorId, date, activeStatuses);
        for (Appointment existing : existingList) {
            if (existing.getPatient().getId().equals(patientId)) {
                throw new IllegalArgumentException("You already have an active appointment booked with this doctor for this date.");
            }
        }

        // 3. Concurrency-safe DailyQueue retrieval and pessimistic locking
        DailyQueue queue = dailyQueueRepository.findByDoctorIdAndQueueDate(doctorId, date)
                .orElseGet(() -> {
                    DailyQueue newQueue = new DailyQueue(doctor, date);
                    try {
                        return dailyQueueRepository.saveAndFlush(newQueue);
                    } catch (Exception e) {
                        return dailyQueueRepository.findByDoctorIdAndQueueDate(doctorId, date)
                                .orElseThrow(() -> new IllegalStateException("Failed to initialize doctor queue."));
                    }
                });

        DailyQueue lockedQueue = dailyQueueRepository.findByDoctorIdAndQueueDateWithLock(doctorId, date)
                .orElseThrow(() -> new IllegalStateException("Could not acquire lock on queue metadata."));

        int tokenAllocated = lockedQueue.getTotalTokens() + 1;
        lockedQueue.setTotalTokens(tokenAllocated);
        dailyQueueRepository.save(lockedQueue);

        // 4. Save the appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(date);
        appointment.setTokenNumber(tokenAllocated);
        appointment.setStatus(date.equals(LocalDate.now()) ? AppointmentStatus.WAITING : AppointmentStatus.BOOKED);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 5. Send confirmation email asynchronously
        emailService.sendBookingConfirmation(savedAppointment);

        return savedAppointment;
    }

    /**
     * Cancel an appointment.
     */
    @Transactional
    public void cancelAppointment(Long appointmentId, String userEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getPatient().getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new SecurityException("Unauthorized action.");
        }

        if (appointment.getStatus() != AppointmentStatus.BOOKED && appointment.getStatus() != AppointmentStatus.WAITING) {
            throw new IllegalArgumentException("Cannot cancel an appointment that has already started or completed.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setNotifiedTurn(true);
        appointmentRepository.save(appointment);
    }

    /**
     * Get booking history for a patient.
     */
    public List<Appointment> getPatientHistory(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    /**
     * Calculate how many patients are ahead of the given appointment.
     */
    public int calculatePatientsAhead(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || 
            appointment.getStatus() == AppointmentStatus.CANCELLED || 
            appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            return 0;
        }

        List<AppointmentStatus> activeQueueStatuses = Arrays.asList(
                AppointmentStatus.WAITING,
                AppointmentStatus.CALLED,
                AppointmentStatus.IN_PROGRESS
        );

        List<Appointment> aheadList = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusIn(
                appointment.getDoctor().getId(),
                appointment.getAppointmentDate(),
                activeQueueStatuses
        );

        int count = 0;
        for (Appointment appt : aheadList) {
            if (appt.getTokenNumber() < appointment.getTokenNumber()) {
                count++;
            }
        }

        return count;
    }
}
