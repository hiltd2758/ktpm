package com.e_health_care.web.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    public String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be 8-50 characters")
    public String password;

    private String role = "ROLE_ADMIN";

    public String getUppercase_role() {
        return this.role.toUpperCase();
    }
}