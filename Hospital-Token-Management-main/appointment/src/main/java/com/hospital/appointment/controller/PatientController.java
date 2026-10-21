package com.hospital.appointment.controller;

import com.hospital.appointment.entity.*;
import com.hospital.appointment.repository.DoctorScheduleRepository;
import com.hospital.appointment.repository.PatientRepository;
import com.hospital.appointment.service.AppointmentService;
import com.hospital.appointment.service.DoctorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Controller to handle all Patient-facing portal routes.
 * Requires ROLE_PATIENT (enforced via SecurityConfig).
 */
@Controller
@RequestMapping("/patient")
public class PatientController {

    private final PatientRepository patientRepository;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final DoctorScheduleRepository doctorScheduleRepository;

    public PatientController(PatientRepository patientRepository,
                             AppointmentService appointmentService,
                             DoctorService doctorService,
                             DoctorScheduleRepository doctorScheduleRepository) {
        this.patientRepository = patientRepository;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.doctorScheduleRepository = doctorScheduleRepository;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Resolve the logged-in user's Patient profile from Spring Security context.
     */
    private Patient resolvePatient(Authentication auth) {
        return patientRepository.findByUserEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Patient profile not found for: " + auth.getName()));
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    /**
     * Patient dashboard: displays active/upcoming appointments and a quick summary.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Patient patient = resolvePatient(auth);
        List<Appointment> allAppointments = appointmentService.getPatientHistory(patient.getId());

        // Split into active (BOOKED/WAITING/CALLED/IN_PROGRESS) vs. past (COMPLETED/CANCELLED/NO_SHOW)
        List<AppointmentStatus> activeStatuses = Arrays.asList(
                AppointmentStatus.BOOKED,
                AppointmentStatus.WAITING,
                AppointmentStatus.CALLED,
                AppointmentStatus.IN_PROGRESS
        );

        List<Appointment> activeAppointments = allAppointments.stream()
                .filter(a -> activeStatuses.contains(a.getStatus()))
                .toList();

        model.addAttribute("patient", patient);
        model.addAttribute("activeAppointments", activeAppointments);
        model.addAttribute("totalBookings", allAppointments.size());
        model.addAttribute("today", LocalDate.now());

        // Pre-calculate patients-ahead for each active appointment
        model.addAttribute("appointmentService", appointmentService);

        return "patient/dashboard";
    }

    // ─── Book Appointment ────────────────────────────────────────────────────

    /**
     * Show the booking form with list of available doctors.
     */
    @GetMapping("/book")
    public String showBookingForm(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("today", LocalDate.now());
        return "patient/book";
    }

    /**
     * Submit a new appointment booking.
     */
    @PostMapping("/book")
    public String bookAppointment(@RequestParam("doctorId") Long doctorId,
                                  @RequestParam("appointmentDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        Patient patient = resolvePatient(auth);

        try {
            Appointment appointment = appointmentService.bookAppointment(patient.getId(), doctorId, appointmentDate);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Appointment booked! Your queue token is #" + appointment.getTokenNumber() + ".");
            return "redirect:/patient/dashboard";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patient/book";
        }
    }

    // ─── Appointment History ─────────────────────────────────────────────────

    /**
     * Show the full appointment history for the logged-in patient.
     */
    @GetMapping("/history")
    public String appointmentHistory(Authentication auth, Model model) {
        Patient patient = resolvePatient(auth);
        List<Appointment> history = appointmentService.getPatientHistory(patient.getId());
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", history);
        return "patient/history";
    }

    // ─── Cancel Appointment ──────────────────────────────────────────────────

    /**
     * Cancel an existing BOOKED or WAITING appointment.
     */
    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable("id") Long appointmentId,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelAppointment(appointmentId, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Appointment successfully cancelled.");
        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/patient/dashboard";
    }

    // ─── Live Queue Tracker ──────────────────────────────────────────────────

    /**
     * Show the real-time live queue tracker for a specific appointment.
     * Connects via WebSocket (STOMP) for push-based live updates.
     */
    @GetMapping("/queue/{id}")
    public String queueTracker(@PathVariable("id") Long appointmentId,
                               Authentication auth,
                               Model model) {
        Patient patient = resolvePatient(auth);

        // Ensure the appointment belongs to this patient
        List<Appointment> patientAppointments = appointmentService.getPatientHistory(patient.getId());
        Appointment appointment = patientAppointments.stream()
                .filter(a -> a.getId().equals(appointmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found or not authorized."));

        int patientsAhead = appointmentService.calculatePatientsAhead(appointment);

        model.addAttribute("appointment", appointment);
        model.addAttribute("patientsAhead", patientsAhead);
        model.addAttribute("patient", patient);

        return "patient/queue-tracker";
    }
}
