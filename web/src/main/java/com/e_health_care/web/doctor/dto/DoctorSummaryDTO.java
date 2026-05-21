package com.e_health_care.web.doctor.dto;

import lombok.Data;

@Data
public class DoctorSummaryDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
}
