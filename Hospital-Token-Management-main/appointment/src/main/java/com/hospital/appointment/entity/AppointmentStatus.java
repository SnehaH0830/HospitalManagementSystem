package com.hospital.appointment.entity;

/**
 * Enumeration representing the possible states of an appointment.
 */
public enum AppointmentStatus {
    /**
     * Appointment is successfully booked but not yet active in today's check-in line.
     */
    BOOKED,

    /**
     * Patient has arrived or the appointment date is today, placing them in the waiting queue.
     */
    WAITING,

    /**
     * The doctor has clicked 'Call Next' and this patient is currently being called.
     */
    CALLED,

    /**
     * The patient is in the consultation room with the doctor.
     */
    IN_PROGRESS,

    /**
     * The consultation has successfully concluded.
     */
    COMPLETED,

    /**
     * The patient or admin has cancelled the appointment.
     */
    CANCELLED,

    /**
     * The patient was called but did not show up.
     */
    NO_SHOW
}
