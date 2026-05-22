package com.e_health_care.web.patient.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequestDTO {
    private Long patientId;
    private Long doctorId;
    private LocalDateTime scheduleTime;
}
