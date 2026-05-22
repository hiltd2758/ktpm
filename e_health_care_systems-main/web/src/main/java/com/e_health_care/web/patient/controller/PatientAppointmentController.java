package com.e_health_care.web.patient.controller;

import com.e_health_care.web.patient.dto.AppointmentRequestDTO;
import com.e_health_care.web.patient.model.Appointment;
import com.e_health_care.web.patient.model.Patient; // Import model Patient
import com.e_health_care.web.patient.model.PatientPrinciple;
import com.e_health_care.web.patient.repository.PatientRepository; // Import Repository
import com.e_health_care.web.patient.service.PatientAppointmentService;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/patient/appointment")
public class PatientAppointmentController {

    @Autowired
    private PatientAppointmentService appointmentService;

    @Autowired
    private DoctorRepository doctorRepository;

    // 1. Thêm Repository để tìm thông tin patient cho Header
    @Autowired
    private PatientRepository patientRepository;

    private Long getCurrentPatientId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof PatientPrinciple principle) {
            return principle.getPatient().getId();
        }
        return null;
    }

    @GetMapping("/book")
    public String bookForm(Model model) {
        Long patientId = getCurrentPatientId();
        if (patientId == null) {
            return "redirect:/patient/login"; // Redirect nếu chưa đăng nhập
        }

        AppointmentRequestDTO requestDTO = new AppointmentRequestDTO();
        requestDTO.setPatientId(patientId);
        model.addAttribute("request", requestDTO);

        // Load danh sách bác sĩ
        List<Doctor> doctors = doctorRepository.findAll();
        model.addAttribute("doctors", doctors);

        // 2. [QUAN TRỌNG] Thêm Patient vào model để Header hoạt động
        Patient patient = patientRepository.findById(patientId).orElse(null);
        model.addAttribute("patient", patient);

        // 3. Sửa đường dẫn đúng thư mục
        return "patient/patient-appointment-book";
    }

    @PostMapping("/book")
    public String bookAppointment(@ModelAttribute AppointmentRequestDTO requestDTO, Model model) {
        Long patientId = getCurrentPatientId();
        requestDTO.setPatientId(patientId);

        try {
            appointmentService.bookAppointment(requestDTO);
            model.addAttribute("success", "Đặt lịch thành công!");
            return "redirect:/patient/appointment/list";
        } catch (RuntimeException e) {
            // == XỬ LÝ KHI CÓ LỖI (Ví dụ: Trùng lịch) ==

            model.addAttribute("error", e.getMessage());

            // Reload danh sách bác sĩ để user chọn lại
            List<Doctor> doctors = doctorRepository.findAll();
            model.addAttribute("doctors", doctors);
            model.addAttribute("request", requestDTO);

            // 4. [QUAN TRỌNG] Cũng phải thêm Patient vào model ở đây
            // Vì khi return về lại trang book, Header sẽ được render lại
            Patient patient = patientRepository.findById(patientId).orElse(null);
            model.addAttribute("patient", patient);

            // 5. Sửa đường dẫn đúng thư mục
            return "patient/patient-appointment-book";
        }
    }

    @GetMapping("/list")
    public String listAppointments(Model model) {
        System.out.println("✅ listAppointments() đã được gọi!");
        Long patientId = getCurrentPatientId();

        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patientId);
        model.addAttribute("appointments", appointments);

        // 6. Đảm bảo trang list cũng có patient (như bạn đã sửa trước đó)
        Patient patient = patientRepository.findById(patientId).orElse(null);
        model.addAttribute("patient", patient);

        return "patient/patient-appointment-list";
    }
}