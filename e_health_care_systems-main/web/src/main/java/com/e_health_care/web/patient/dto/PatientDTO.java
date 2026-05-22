package com.e_health_care.web.patient.dto;

import java.time.LocalDate;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PatientDTO {
    private long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String address;
    private String phone;

    private String role = "patient";

    public String getUppercase_role() {
        return this.role.toUpperCase();
    }

    private LocalDate dateOfBirth; // Example field
    private String medicalHistory; // Example field for records
    private String avatar;
    private MultipartFile avatarFile; // Trường này dùng để upload

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
