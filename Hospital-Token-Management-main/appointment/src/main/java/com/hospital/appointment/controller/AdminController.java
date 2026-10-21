package com.hospital.appointment.controller;

import com.hospital.appointment.dto.DoctorDto;
import com.hospital.appointment.dto.ScheduleFormWrapper;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.AppointmentStatus;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.Specialization;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.PatientRepository;
import com.hospital.appointment.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller to handle Admin activities (Doctors CRUD, schedules configuration, specializations).
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DoctorService doctorService;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public AdminController(DoctorService doctorService, PatientRepository patientRepository,
                           AppointmentRepository appointmentRepository) {
        this.doctorService = doctorService;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Admin Dashboard: statistics and all appointments list.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalDoctors", doctorService.getAllDoctors().size());
        model.addAttribute("totalSpecializations", doctorService.getAllSpecializations().size());
        model.addAttribute("totalPatients", patientRepository.count());
        model.addAttribute("totalAppointments", appointmentRepository.count());

        // Status-specific counts
        model.addAttribute("countCompleted", appointmentRepository.countByStatus(AppointmentStatus.COMPLETED));
        model.addAttribute("countCancelled", appointmentRepository.countByStatus(AppointmentStatus.CANCELLED));
        model.addAttribute("countNoShow", appointmentRepository.countByStatus(AppointmentStatus.NO_SHOW));
        
        long activeCount = appointmentRepository.countByStatus(AppointmentStatus.BOOKED)
                + appointmentRepository.countByStatus(AppointmentStatus.WAITING)
                + appointmentRepository.countByStatus(AppointmentStatus.CALLED)
                + appointmentRepository.countByStatus(AppointmentStatus.IN_PROGRESS);
        model.addAttribute("countActive", activeCount);

        List<Appointment> allAppointments = appointmentRepository.findAll();
        model.addAttribute("appointments", allAppointments);

        return "admin/dashboard";
    }

    /**
     * Specializations list and creation form.
     */
    @GetMapping("/specializations")
    public String specializations(Model model) {
        model.addAttribute("specializations", doctorService.getAllSpecializations());
        model.addAttribute("specialization", new Specialization());
        return "admin/specializations";
    }

    @PostMapping("/specializations")
    public String addSpecialization(@ModelAttribute("specialization") Specialization specialization, Model model) {
        try {
            doctorService.saveSpecialization(specialization);
            return "redirect:/admin/specializations?success=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("specializations", doctorService.getAllSpecializations());
            return "admin/specializations";
        }
    }

    /**
     * Doctors list.
     */
    @GetMapping("/doctors")
    public String doctors(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        return "admin/doctors";
    }

    /**
     * Doctor Create Form.
     */
    @GetMapping("/doctors/new")
    public String newDoctorForm(Model model) {
        model.addAttribute("doctor", new DoctorDto());
        model.addAttribute("specializations", doctorService.getAllSpecializations());
        model.addAttribute("isEdit", false);
        return "admin/doctor-form";
    }

    @PostMapping("/doctors/new")
    public String saveDoctor(@Valid @ModelAttribute("doctor") DoctorDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("specializations", doctorService.getAllSpecializations());
            model.addAttribute("isEdit", false);
            return "admin/doctor-form";
        }

        try {
            doctorService.createDoctor(dto);
            return "redirect:/admin/doctors?created=true";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.doctor", e.getMessage());
            model.addAttribute("specializations", doctorService.getAllSpecializations());
            model.addAttribute("isEdit", false);
            return "admin/doctor-form";
        }
    }

    /**
     * Doctor Edit Form.
     */
    @GetMapping("/doctors/edit/{id}")
    public String editDoctorForm(@PathVariable("id") Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        
        DoctorDto dto = new DoctorDto();
        dto.setId(doctor.getId());
        dto.setEmail(doctor.getUser().getEmail());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setPhone(doctor.getPhone());
        dto.setRoomNumber(doctor.getRoomNumber());
        dto.setSpecializationId(doctor.getSpecialization().getId());
        dto.setAvgConsultationTime(doctor.getAvgConsultationTime());

        model.addAttribute("doctor", dto);
        model.addAttribute("specializations", doctorService.getAllSpecializations());
        model.addAttribute("isEdit", true);
        return "admin/doctor-form";
    }

    @PostMapping("/doctors/edit/{id}")
    public String updateDoctor(@PathVariable("id") Long id, @Valid @ModelAttribute("doctor") DoctorDto dto,
                               BindingResult result, Model model) {
        dto.setId(id); // ensure route ID is bound
        if (result.hasErrors()) {
            model.addAttribute("specializations", doctorService.getAllSpecializations());
            model.addAttribute("isEdit", true);
            return "admin/doctor-form";
        }

        try {
            doctorService.updateDoctor(dto);
            return "redirect:/admin/doctors?updated=true";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.doctor", e.getMessage());
            model.addAttribute("specializations", doctorService.getAllSpecializations());
            model.addAttribute("isEdit", true);
            return "admin/doctor-form";
        }
    }

    /**
     * Delete Doctor profile.
     */
    @GetMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable("id") Long id) {
        try {
            doctorService.deleteDoctor(id);
            return "redirect:/admin/doctors?deleted=true";
        } catch (Exception e) {
            return "redirect:/admin/doctors?error=true";
        }
    }

    /**
     * Edit doctor weekly availability schedule.
     */
    @GetMapping("/doctors/schedule/{id}")
    public String editSchedule(@PathVariable("id") Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        ScheduleFormWrapper wrapper = new ScheduleFormWrapper(doctorService.getSchedulesByDoctorId(id));
        
        model.addAttribute("wrapper", wrapper);
        model.addAttribute("doctor", doctor);
        return "admin/doctor-schedule";
    }

    @PostMapping("/doctors/schedule/{id}")
    public String saveSchedule(@PathVariable("id") Long id, @ModelAttribute("wrapper") ScheduleFormWrapper wrapper) {
        doctorService.saveDoctorSchedules(id, wrapper.getSchedules());
        return "redirect:/admin/doctors?scheduleUpdated=true";
    }
}
