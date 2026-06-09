package com.e_health_care.web.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminDTO {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    public String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, max = 50, message = "Password phải từ 8 đến 50 ký tự")
    public String password;

    private String role = "ROLE_ADMIN";

    public String getUppercase_role() {
        return this.role.toUpperCase();
    }
}