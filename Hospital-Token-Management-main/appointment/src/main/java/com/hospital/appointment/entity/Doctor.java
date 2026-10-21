package com.hospital.appointment.entity;

import jakarta.persistence.*;

/**
 * Entity representing a Doctor profile.
 */
@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialization;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(name = "avg_consultation_time", nullable = false)
    private int avgConsultationTime = 8; // average consultation time in minutes

    public Doctor() {
    }

    public Doctor(User user, Specialization specialization, String firstName, String lastName, String phone, String roomNumber, int avgConsultationTime) {
        this.user = user;
        this.specialization = specialization;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.roomNumber = roomNumber;
        this.avgConsultationTime = avgConsultationTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
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

    public int getAvgConsultationTime() {
        return avgConsultationTime;
    }

    public void setAvgConsultationTime(int avgConsultationTime) {
        this.avgConsultationTime = avgConsultationTime;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", avgConsultationTime=" + avgConsultationTime +
                '}';
    }
}
