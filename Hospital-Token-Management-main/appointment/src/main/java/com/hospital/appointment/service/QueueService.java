package com.hospital.appointment.service;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.AppointmentStatus;
import com.hospital.appointment.entity.DailyQueue;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.DailyQueueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service to manage active queues, patient transitions, and status updates.
 */
@Service
public class QueueService {

    private final AppointmentRepository appointmentRepository;
    private final DailyQueueRepository dailyQueueRepository;
    private final EmailService emailService;

    public QueueService(AppointmentRepository appointmentRepository, DailyQueueRepository dailyQueueRepository,
                        EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.dailyQueueRepository = dailyQueueRepository;
        this.emailService = emailService;
    }

    /**
     * Call the next patient in line for a doctor on a specific date.
     * 
     * Handles:
     * 1. Concluding the previously active patient (CALLED or IN_PROGRESS -> COMPLETED).
     * 2. Finding the next waiting patient in chronological order (lowest token number).
     * 3. Transitioning the next patient to CALLED status.
     * 4. Updating the DailyQueue current serving token index.
     * 5. Sending email notifications (Your Turn for the called patient, and Two-Ahead alerts for subsequent patients).
     */
    @Transactional
    public Optional<Appointment> callNextPatient(Long doctorId, LocalDate date) {
        // Acquire write lock to prevent race conditions on queue operations
        DailyQueue queue = dailyQueueRepository.findByDoctorIdAndQueueDateWithLock(doctorId, date)
                .orElseThrow(() -> new IllegalStateException("Queue not initialized for doctor on this date."));

        // 1. Auto-complete any currently active session (CALLED or IN_PROGRESS)
        List<AppointmentStatus> activeConsultationStatuses = Arrays.asList(
                AppointmentStatus.CALLED,
                AppointmentStatus.IN_PROGRESS
        );
        List<Appointment> activeConsultations = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusIn(
                doctorId, date, activeConsultationStatuses
        );
        for (Appointment active : activeConsultations) {
            active.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(active);
        }

        // 2. Locate the next waiting patient (status = WAITING) sorted by token number ascending
        List<Appointment> waitingPatients = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusIn(
                doctorId, date, Arrays.asList(AppointmentStatus.WAITING)
        );

        Optional<Appointment> nextPatientOpt = waitingPatients.stream()
                .min(Comparator.comparingInt(Appointment::getTokenNumber));

        if (nextPatientOpt.isPresent()) {
            Appointment nextPatient = nextPatientOpt.get();
            
            // Transition to CALLED
            nextPatient.setStatus(AppointmentStatus.CALLED);
            nextPatient.setNotifiedTurn(true);
            appointmentRepository.save(nextPatient);

            // Trigger "Your Turn" Email asynchronously
            emailService.sendYourTurnAlert(nextPatient);

            // Update queue current active token
            queue.setCurrentToken(nextPatient.getTokenNumber());
            dailyQueueRepository.save(queue);

            // 3. Process alerts for subsequent waiting patients in line (Two patients ahead)
            // Fetch updated list of all active queue items (WAITING, CALLED, IN_PROGRESS)
            List<Appointment> activeQueue = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusIn(
                    doctorId, date, Arrays.asList(AppointmentStatus.WAITING, AppointmentStatus.CALLED, AppointmentStatus.IN_PROGRESS)
            );

            for (Appointment appt : activeQueue) {
                if (appt.getStatus() == AppointmentStatus.WAITING) {
                    int aheadCount = calculateAhead(appt, activeQueue);
                    if (aheadCount <= 2 && !appt.isNotifiedTwoAhead()) {
                        appt.setNotifiedTwoAhead(true);
                        appointmentRepository.save(appt);
                        
                        // Send "Two Patients Ahead" Email asynchronously
                        emailService.sendTwoPatientsAheadAlert(appt);
                    }
                }
            }

            return Optional.of(nextPatient);
        }

        return Optional.empty(); // No more patients waiting
    }

    /**
     * Mark an appointment as completed.
     */
    @Transactional
    public void markCompleted(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.CALLED && appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Can only complete active consultations.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
    }

    /**
     * Mark an appointment as no-show.
     */
    @Transactional
    public void markNoShow(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.CALLED && appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Can only mark active consultations as no-show.");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointmentRepository.save(appointment);
    }

    /**
     * Retrieve the queue metadata for a doctor and date.
     */
    public Optional<DailyQueue> getDailyQueue(Long doctorId, LocalDate date) {
        return dailyQueueRepository.findByDoctorIdAndQueueDate(doctorId, date);
    }

    /**
     * Calculate patients ahead of the target appointment in the active list.
     */
    private int calculateAhead(Appointment target, List<Appointment> activeQueue) {
        int count = 0;
        for (Appointment appt : activeQueue) {
            if (appt.getTokenNumber() < target.getTokenNumber()) {
                count++;
            }
        }
        return count;
    }
}
