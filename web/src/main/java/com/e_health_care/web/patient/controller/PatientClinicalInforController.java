package com.e_health_care.web.patient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.e_health_care.web.patient.dto.PatientClinicalInforDTO;
import com.e_health_care.web.patient.model.Patient; // Import model Patient
import com.e_health_care.web.patient.repository.PatientRepository; // Import Repository
import com.e_health_care.web.patient.service.PatientRecordService;

@Controller
@RequestMapping("/patient")
public class PatientClinicalInforController {

    @Autowired
    private PatientRecordService patientRecordService;

    // Thêm Repository để tìm thông tin patient
    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/clinical-infor/{id}")
    public String showPatientClient(@PathVariable("id") Long id, Model model) {
        PatientClinicalInforDTO dto = patientRecordService.getClinicalForEdit(id);
        dto.setPatientId(id);

        // 2. [QUAN TRỌNG] Lấy thông tin Patient để hiển thị Header
        // Header cần biến ${patient} để render link Medical Records
        Patient patient = patientRepository.findById(id).orElse(new Patient());
        // Hoặc nếu DTO đã có đủ thông tin, bạn có thể map từ DTO,
        // nhưng an toàn nhất là query lấy object Patient gốc.

        // 3. Đưa vào Model
        model.addAttribute("clinicalInforDTO", dto);
        model.addAttribute("patient", patient);
        model.addAttribute("isDoctorView", false);

        // Trả về view (nhớ đường dẫn folder patient/)
        return "patient/patient-medical-records";
    }
}