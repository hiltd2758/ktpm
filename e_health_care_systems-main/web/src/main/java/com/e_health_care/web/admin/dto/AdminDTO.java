package com.e_health_care.web.admin.dto;

import lombok.Data;

@Data
public class AdminDTO {
    public String email;
    public String password;
    private String role = "ROLE_ADMIN";
    public String getUppercase_role() {
        return this.role.toUpperCase();
    }
}
