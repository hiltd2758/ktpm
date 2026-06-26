    package com.e_health_care.web.doctor.dto;

    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.Size;
    import org.springframework.web.multipart.MultipartFile;
    import lombok.Data;
    import com.fasterxml.jackson.annotation.JsonIgnore;


    @Data
    public class DoctorDTO {
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        private String role = "doctor";

        private Long id;

        @Size(min = 1, max = 50)
        private String firstName;

        @Size(min = 1, max = 50)
        private String lastName;

        @Size(max = 10)
        private String phone;

        private String avatar;

        private String field;


        private String address;

        private MultipartFile avatarFile;

        @JsonIgnore
        public String getUppercase_role() {
            return this.role.toUpperCase();
        }
    }
