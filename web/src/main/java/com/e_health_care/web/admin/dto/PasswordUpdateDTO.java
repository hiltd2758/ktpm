package com.e_health_care.web.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating patient password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateDTO {
    private String newPassword;
}
