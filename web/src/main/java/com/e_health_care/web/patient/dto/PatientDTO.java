package com.e_health_care.web.patient.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PatientDTO {
    private long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String address;

    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Invalid phone number format")
    private String phone;

    private String role = "patient";

    public String getUppercase_role() {
        return this.role.toUpperCase();
    }

    @Past(message = "Date of birth must be a date in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth; 
    private String medicalHistory; 
    private String avatar;
    private MultipartFile avatarFile; 

    // Getters và Setters
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public MultipartFile getAvatarFile() {
        return avatarFile;
    }

    public void setAvatarFile(MultipartFile avatarFile) {
        this.avatarFile = avatarFile;
    }
}
