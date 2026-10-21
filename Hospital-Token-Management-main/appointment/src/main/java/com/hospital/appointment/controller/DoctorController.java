package com.hospital.appointment.controller;

import com.hospital.appointment.dto.QueueUpdateDto;
import com.hospital.appointment.entity.*;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.DoctorRepository;
import com.hospital.appointment.service.QueueService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Controller to handle all Doctor-facing portal routes.
 * Requires ROLE_DOCTOR (enforced via SecurityConfig).
 */
@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final QueueService queueService;
    private final SimpMessagingTemplate messagingTemplate;

    public DoctorController(DoctorRepository doctorRepository,
                             AppointmentRepository appointmentRepository,
                             QueueService queueService,
                             SimpMessagingTemplate messagingTemplate) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.queueService = queueService;
        this.messagingTemplate = messagingTemplate;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Resolve the logged-in user's Doctor profile from Spring Security context.
     */
    private Doctor resolveDoctor(Authentication auth) {
        return doctorRepository.findByUserEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Doctor profile not found for: " + auth.getName()));
    }

    /**
     * Broadcast real-time queue state update to all STOMP subscribers on /topic/queue-updates.
     */
    private void broadcastQueueUpdate(Long doctorId, LocalDate date) {
        Optional<com.hospital.appointment.entity.DailyQueue> queueOpt =
                queueService.getDailyQueue(doctorId, date);

        int currentToken = queueOpt.map(com.hospital.appointment.entity.DailyQueue::getCurrentToken).orElse(0);
        int totalTokens = queueOpt.map(com.hospital.appointment.entity.DailyQueue::getTotalTokens).orElse(0);

        QueueUpdateDto update = new QueueUpdateDto(doctorId, date.toString(), currentToken, totalTokens);
        messagingTemplate.convertAndSend("/topic/queue-updates", update);
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    /**
     * Doctor dashboard: shows today's complete queue with all patient statuses.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Doctor doctor = resolveDoctor(auth);
        LocalDate today = LocalDate.now();

        // Fetch all of today's appointments for this doctor
        List<Appointment> todayAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentDate(doctor.getId(), today);

        // Sort by token number for correct queue display
        todayAppointments.sort(Comparator.comparingInt(Appointment::getTokenNumber));

        // Find the currently active appointment (CALLED or IN_PROGRESS) if any
        List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.CALLED, AppointmentStatus.IN_PROGRESS
        );
        Optional<Appointment> activeAppointment = todayAppointments.stream()
                .filter(a -> activeStatuses.contains(a.getStatus()))
                .findFirst();

        // Count patients still waiting
        long waitingCount = todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.WAITING)
                .count();
        long completedCount = todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();

        model.addAttribute("doctor", doctor);
        model.addAttribute("today", today);
        model.addAttribute("todayAppointments", todayAppointments);
        model.addAttribute("activeAppointment", activeAppointment.orElse(null));
        model.addAttribute("waitingCount", waitingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalToday", todayAppointments.size());

        return "doctor/dashboard";
    }

    // ─── Queue Actions ────────────────────────────────────────────────────────

    /**
     * Call the next WAITING patient in the queue.
     * Also broadcasts a WebSocket STOMP message so patient trackers update live.
     */
    @PostMapping("/queue/call-next")
    public String callNext(Authentication auth, RedirectAttributes redirectAttributes) {
        Doctor doctor = resolveDoctor(auth);
        LocalDate today = LocalDate.now();

        try {
            Optional<Appointment> nextOpt = queueService.callNextPatient(doctor.getId(), today);

            if (nextOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Token #" + nextOpt.get().getTokenNumber() + " called — " +
                        nextOpt.get().getPatient().getFirstName() + " " + nextOpt.get().getPatient().getLastName() + ".");
            } else {
                redirectAttributes.addFlashAttribute("infoMessage",
                        "No more patients are waiting in today's queue.");
            }

            // Broadcast live update after state change
            broadcastQueueUpdate(doctor.getId(), today);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/doctor/dashboard";
    }

    /**
     * Mark the currently CALLED or IN_PROGRESS appointment as COMPLETED.
     */
    @PostMapping("/queue/complete/{id}")
    public String markComplete(@PathVariable("id") Long appointmentId,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        Doctor doctor = resolveDoctor(auth);

        try {
            queueService.markCompleted(appointmentId);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment marked as Completed.");
            broadcastQueueUpdate(doctor.getId(), LocalDate.now());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/doctor/dashboard";
    }

    /**
     * Mark the currently CALLED or IN_PROGRESS appointment as NO_SHOW.
     */
    @PostMapping("/queue/no-show/{id}")
    public String markNoShow(@PathVariable("id") Long appointmentId,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        Doctor doctor = resolveDoctor(auth);

        try {
            queueService.markNoShow(appointmentId);
            redirectAttributes.addFlashAttribute("warningMessage", "Appointment marked as No-Show.");
            broadcastQueueUpdate(doctor.getId(), LocalDate.now());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/doctor/dashboard";
    }
}
