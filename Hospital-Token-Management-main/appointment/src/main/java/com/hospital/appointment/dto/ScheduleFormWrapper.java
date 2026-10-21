package com.hospital.appointment.dto;

import com.hospital.appointment.entity.DoctorSchedule;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper DTO for binding lists of DoctorSchedule items in Thymeleaf forms.
 */
public class ScheduleFormWrapper {

    private List<DoctorSchedule> schedules = new ArrayList<>();

    public ScheduleFormWrapper() {
    }

    public ScheduleFormWrapper(List<DoctorSchedule> schedules) {
        this.schedules = schedules;
    }

    public List<DoctorSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<DoctorSchedule> schedules) {
        this.schedules = schedules;
    }
}
