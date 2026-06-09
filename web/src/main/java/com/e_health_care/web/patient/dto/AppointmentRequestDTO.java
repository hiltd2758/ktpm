package com.e_health_care.web.patient.dto;

import lombok.Data;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

@Data
public class AppointmentRequestDTO {
    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Schedule time is required")
    @Future(message = "Schedule time must be in the future")
    private LocalDateTime scheduleTime;
}
