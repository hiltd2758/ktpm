package com.e_health_care.web.doctor.controller;

import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.model.Appointment;
import com.e_health_care.web.patient.service.PatientAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorCheckingAppointmentController {

    @Autowired
    private PatientAppointmentService patientAppointmentService;

    // 1. Thêm Repository để lấy thông tin Doctor cho Header
    @Autowired
    private DoctorRepository doctorRepository;

    // Hàm bổ trợ lấy bác sĩ hiện tại (giúp code gọn hơn)
    private Doctor getCurrentDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return doctorRepository.findByEmail(auth.getName());
    }

    // 2. Sửa đường dẫn mapping cho thống nhất
    @GetMapping("/appointment/list")
    public String getAppointmentFromPatient(Model model) {
        Doctor doctor = getCurrentDoctor();

        // Kiểm tra đăng nhập
        if (doctor == null) return "redirect:/doctor/login";

        List<Appointment> appointment = patientAppointmentService.getAppointmentsByDoctor(doctor.getId());
        model.addAttribute("doctorAppointment", appointment);

        // 3. [QUAN TRỌNG] Thêm dòng này để Header không bị lỗi
        model.addAttribute("doctor", doctor);

        return "doctor/doctor-appointment-check";
    }

    @PostMapping("/appointment/update/{id}/{status}")
    public String updateAppointmentStatus(@PathVariable Long id, @PathVariable String status) {
        patientAppointmentService.updateAppointmentStatus(id, status);

        // 4. [QUAN TRỌNG] Sửa đường dẫn redirect cho khớp với @GetMapping ở trên
        // Cũ (Sai): return "redirect:/doctor/appointment";
        // Mới (Đúng):
        return "redirect:/doctor/appointment/list";
    }
}