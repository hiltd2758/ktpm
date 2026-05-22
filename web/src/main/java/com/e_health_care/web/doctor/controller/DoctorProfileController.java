package com.e_health_care.web.doctor.controller;

import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/doctor")
public class DoctorProfileController {

    @Autowired
    private DoctorService doctorDetailsService;

    // 1. Hiển thị trang hồ sơ
    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        String email = principal.getName();
        DoctorDTO currentDoctor = doctorDetailsService.getDoctorByEmail(email);

        model.addAttribute("doctor", currentDoctor);

        return "doctor/doctor-profile";
    }

    // 2. Hiển thị trang sửa hồ sơ
    @GetMapping("/profile/edit")
    public String showUpdateProfileForm(Model model, Principal principal) {
        String email = principal.getName();
        DoctorDTO currentDoctor = doctorDetailsService.getDoctorByEmail(email);
        model.addAttribute("doctorDTO", currentDoctor);
        // --------------------

        return "doctor/doctor-update-profile";
    }

    // 3. Xử lý hành động cập nhật hồ sơ
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("doctorDTO") DoctorDTO doctorDTO,
                                RedirectAttributes redirectAttributes) {
        doctorDetailsService.updateDoctorProfile(doctorDTO);
        redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");
        return "redirect:/doctor/profile";
    }
}