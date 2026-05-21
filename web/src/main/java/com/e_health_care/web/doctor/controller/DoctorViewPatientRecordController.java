package com.e_health_care.web.doctor.controller;

import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.doctor.service.DoctorViewPatientService;
import com.e_health_care.web.patient.dto.PatientClinicalInforDTO;
import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.dto.PatientSummaryDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorViewPatientRecordController {

    @Autowired
    private DoctorViewPatientService doctorViewPatientService;

    @Autowired
    private DoctorRepository doctorRepository;

    // Helper: Lấy bác sĩ hiện tại
    private Doctor getCurrentDoctor(Principal principal) {
        if (principal == null) return null;
        return doctorRepository.findByEmail(principal.getName());
    }

    // 1. Dashboard: Danh sách bệnh nhân
    @GetMapping("/dashboard")
    public String viewPatientClinical(Model model, HttpServletRequest request, Principal principal) {
        Doctor doctor = getCurrentDoctor(principal);
        if (doctor == null) return "redirect:/doctor/login";

        List<PatientSummaryDTO> patients = doctorViewPatientService.getAllPatients();
        model.addAttribute("patients", patients);
        model.addAttribute("doctorToken", request.getAttribute("doctorToken"));
        model.addAttribute("doctor", doctor);

        return "doctor/doctor-dashboard";
    }

    // 2. Hiển thị trang chi tiết bệnh án (GET)
    @GetMapping("/patient/{id}")
    public String viewPatientClinicalRecord(@PathVariable("id") Long patientId, Model model, Principal principal) {
        Doctor doctor = getCurrentDoctor(principal);
        if (doctor == null) return "redirect:/doctor/login";
        model.addAttribute("doctor", doctor);

        // Lấy thông tin bệnh nhân
        PatientDTO patientDTO = doctorViewPatientService.getPatientProfile(patientId);
        if (patientDTO == null) {
            return "redirect:/doctor/dashboard?error=PatientNotFound";
        }
        model.addAttribute("patientDTO", patientDTO);

        // Lấy thông tin bệnh án
        PatientClinicalInforDTO clinicalInforDTO = doctorViewPatientService.getPatientClinicalInfo(patientId);
        if (clinicalInforDTO == null) {
            clinicalInforDTO = new PatientClinicalInforDTO();
            clinicalInforDTO.setPatientId(patientId);
        }
        model.addAttribute("clinicalInforDTO", clinicalInforDTO);
        model.addAttribute("isDoctorView", true);

        return "doctor/doctor-medical-records";
    }

    // 3. Xử lý cập nhật bệnh án (POST) - ĐÂY LÀ PHẦN BẠN ĐANG THIẾU
    @PostMapping("/patient/{id}")
    public String updatePatientClinicalRecord(@PathVariable("id") Long patientId,
                                              @ModelAttribute("clinicalInforDTO") PatientClinicalInforDTO clinicalInforDTO,
                                              Principal principal) {

        // Gọi service lưu dữ liệu
        doctorViewPatientService.updatePatientClinicalInfo(patientId, clinicalInforDTO, principal.getName());

        // Redirect lại trang chi tiết kèm thông báo thành công
        return "redirect:/doctor/patient/" + patientId + "?success";
    }
}