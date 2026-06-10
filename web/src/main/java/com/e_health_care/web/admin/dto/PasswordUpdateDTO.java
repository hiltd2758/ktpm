package com.e_health_care.web.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank
    @Size(min = 8, max = 50, message = "Password must be 8-50 characters")
    private String newPassword;
}
