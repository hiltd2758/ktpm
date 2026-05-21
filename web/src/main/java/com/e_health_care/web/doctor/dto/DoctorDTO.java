package com.e_health_care.web.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class DoctorDTO {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String role = "doctor";

    private Long id;

    private String firstName;

    private String lastName;

    private String avatar;

    private String field;

    private String phone;

    private String address;

    private MultipartFile avatarFile;

    public String getUppercase_role() {
        return this.role.toUpperCase();
    }
}
