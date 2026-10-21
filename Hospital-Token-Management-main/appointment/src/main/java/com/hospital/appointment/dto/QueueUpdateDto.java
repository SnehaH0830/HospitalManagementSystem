package com.hospital.appointment.dto;

/**
 * Data Transfer Object containing real-time queue update states.
 */
public class QueueUpdateDto {

    private Long doctorId;
    private String queueDate;
    private int currentToken;
    private int totalTokens;

    public QueueUpdateDto() {
    }

    public QueueUpdateDto(Long doctorId, String queueDate, int currentToken, int totalTokens) {
        this.doctorId = doctorId;
        this.queueDate = queueDate;
        this.currentToken = currentToken;
        this.totalTokens = totalTokens;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getQueueDate() {
        return queueDate;
    }

    public void setQueueDate(String queueDate) {
        this.queueDate = queueDate;
    }

    public int getCurrentToken() {
        return currentToken;
    }

    public void setCurrentToken(int currentToken) {
        this.currentToken = currentToken;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }

    @Override
    public String toString() {
        return "QueueUpdateDto{" +
                "doctorId=" + doctorId +
                ", queueDate='" + queueDate + '\'' +
                ", currentToken=" + currentToken +
                ", totalTokens=" + totalTokens +
                '}';
    }
}
