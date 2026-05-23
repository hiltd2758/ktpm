package com.e_health_care.web.api;

import com.e_health_care.web.patient.dto.AppointmentRequestDTO;
import com.e_health_care.web.patient.model.Appointment;
import com.e_health_care.web.patient.model.PatientPrinciple;
import com.e_health_care.web.patient.service.PatientAppointmentService;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient/appointment")
public class PatientAppointmentApiController {

    @Autowired
    private PatientAppointmentService appointmentService;

    @Autowired
    private DoctorRepository doctorRepository;

    private Long getCurrentPatientId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof PatientPrinciple principle) {
            return principle.getPatient().getId();
        }
        return null;
    }

    @GetMapping("/doctors")
    public ResponseEntity<?> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        return ResponseEntity.ok(doctors);
    }

    @PostMapping("/book")
    public ResponseEntity<?> book(@RequestBody AppointmentRequestDTO requestDTO) {
        Long patientId = getCurrentPatientId();
        if (patientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        requestDTO.setPatientId(patientId);
        try {
            appointmentService.bookAppointment(requestDTO);
            return ResponseEntity.ok(Map.of("message", "Đặt lịch thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        Long patientId = getCurrentPatientId();
        if (patientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(appointments);
    }
}