package com.e_health_care.web.admin.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating patient information (excluding password)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientUpdateDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
    private LocalDate dateOfBirth;
    private String medicalHistory;
    private String avatar;
}
