package com.e_health_care.web.patient.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PatientDTO {
    private Long id; // đổi long → Long

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be 8-50 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50)
    private String lastName;

    private String address;

    @Size(max = 10, message = "Phone max 10 digits")
    private String phone;

    private String role = "patient";

    public String getUppercase_role() {
        return this.role.toUpperCase();
    }

    private LocalDate dateOfBirth;
    private String medicalHistory;
    private String avatar;
    private MultipartFile avatarFile;

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public MultipartFile getAvatarFile() { return avatarFile; }
    public void setAvatarFile(MultipartFile avatarFile) { this.avatarFile = avatarFile; }
}