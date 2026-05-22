package com.e_health_care.web.patient.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientSummaryDTO {
    private long id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String firstName;
    private String lastName;
    private String avatar;
}
