package com.hospital.appointment.controller;

import com.hospital.appointment.dto.UserRegisterDto;
import com.hospital.appointment.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to handle Authentication routing (Login, Patient Registration).
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Show custom login page.
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    /**
     * Show registration form for patients.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegisterDto());
        return "auth/register";
    }

    /**
     * Process registration submission.
     */
    @PostMapping("/register")
    public String registerPatient(@Valid @ModelAttribute("user") UserRegisterDto registerDto,
                                  BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerPatient(registerDto);
            return "redirect:/auth/login?registered=true";
        } catch (IllegalArgumentException e) {
            // Reject value adds a field-specific validation error for the 'email' input field
            result.rejectValue("email", "error.user", e.getMessage());
            return "auth/register";
        }
    }
}
