package com.e_health_care.web.admin.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new patient
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientCreateDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private String password;
    private String phone;
    private LocalDate dateOfBirth;
    private String medicalHistory;
    private String avatar;
}
