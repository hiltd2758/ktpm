package com.e_health_care.web.patient.controller;

import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import com.e_health_care.web.patient.service.PatientUpdateProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Import thêm cái này để truyền message khi redirect

import jakarta.servlet.http.HttpServletRequest;
import java.beans.PropertyEditorSupport;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.WebDataBinder;

@Controller
@RequestMapping("/patient")
public class PatientUpdateProfileController {
    @Autowired private PatientUpdateProfileService patientService;
    @Autowired private PatientRepository patientRepository;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text != null && !text.isEmpty()) {
                    setValue(LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                } else {
                    setValue(null);
                }
            }
        });
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        PatientDTO patientDTO = patientService.getPatientById(id);
        model.addAttribute("patient", patientDTO);
        return "patient/patient-update-profile";
    }

    @PostMapping("/update/{id}")
    public String updatePatient(@PathVariable("id") Long id,
                                @ModelAttribute("patient") PatientDTO patientDTO,
                                RedirectAttributes redirectAttributes) { // Dùng RedirectAttributes thay vì Model
        try {
            patientService.updatePatient(patientDTO, id);
            // Thông báo thành công sẽ được chuyển sang trang sau khi redirect
            redirectAttributes.addFlashAttribute("message", "Updated Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error Update: " + e.getMessage());
        }

        // Redirect về trang Profile để xem kết quả và ảnh mới
        return "redirect:/patient/profile";
    }

    // ... Các hàm index và profile bên dưới giữ nguyên ...
    @GetMapping("/index")
    public String patientIndex(Model model, Principal principal, HttpServletRequest request) {
        if (principal == null) {
            return "redirect:/patient/login";
        }
        String email = principal.getName();
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        model.addAttribute("patient", patient);
        model.addAttribute("patientToken", request.getAttribute("patientToken"));
        return "patient/patient-index";
    }

    @GetMapping("/profile")
    public String showMyProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/patient/login";
        }
        String email = principal.getName();
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        model.addAttribute("patient", patient);
        return "patient/patient-profile";
    }
}