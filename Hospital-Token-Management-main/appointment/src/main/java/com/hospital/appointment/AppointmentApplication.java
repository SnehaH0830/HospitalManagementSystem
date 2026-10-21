package com.hospital.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class to bootstrap the Hospital Token Management System.
 * 
 * We use @EnableAsync to support sending notifications asynchronously in the background.
 */
@SpringBootApplication
@EnableAsync
public class AppointmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppointmentApplication.class, args);
    }
}
