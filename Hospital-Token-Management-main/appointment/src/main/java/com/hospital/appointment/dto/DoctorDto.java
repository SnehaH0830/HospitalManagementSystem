package com.hospital.appointment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for creating or updating a Doctor profile.
 */
public class DoctorDto {

    private Long id; // optional, used for updates

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    private String password; // optional during updates, required during creation

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Specialization is required")
    private Long specializationId;

    @Min(value = 1, message = "Average consultation time must be at least 1 minute")
    private int avgConsultationTime = 8;

    public DoctorDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Long getSpecializationId() {
        return specializationId;
    }

    public void setSpecializationId(Long specializationId) {
        this.specializationId = specializationId;
    }

    public int getAvgConsultationTime() {
        return avgConsultationTime;
    }

    public void setAvgConsultationTime(int avgConsultationTime) {
        this.avgConsultationTime = avgConsultationTime;
    }

    @Override
    public String toString() {
        return "DoctorDto{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", specializationId=" + specializationId +
                ", avgConsultationTime=" + avgConsultationTime +
                '}';
    }
}
