package com.e_health_care.web.doctor.controller;

import com.e_health_care.web.doctor.model.DoctorPrinciple;
import com.e_health_care.web.doctor.dto.DoctorDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.e_health_care.web.doctor.controller")
public class DoctorGlobalController {

    @ModelAttribute("doctor")
    public DoctorDTO populateDoctor(@AuthenticationPrincipal DoctorPrinciple principal) {
        if (principal != null && principal.getDoctor() != null) {
            DoctorDTO dto = new DoctorDTO();
            dto.setId(principal.getDoctor().getId());
            dto.setLastName(principal.getDoctor().getLastName());
            dto.setAvatar(principal.getDoctor().getAvatar());
            return dto;
        }
        return null;
    }
}