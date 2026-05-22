package com.e_health_care.web.patient.service;

import com.e_health_care.web.patient.dto.AppointmentRequestDTO;
import com.e_health_care.web.patient.model.Appointment;
import com.e_health_care.web.patient.repository.PatientAppointmentRepository;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PatientAppointmentService {

    @Autowired
    private PatientAppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    public Appointment bookAppointment(AppointmentRequestDTO request) {
        // Kiểm tra bệnh nhân tồn tại
        Optional<Patient> patientOpt = patientRepository.findById(request.getPatientId());
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Patient not found");
        }

        // Kiểm tra bác sĩ tồn tại
        Optional<Doctor> doctorOpt = doctorRepository.findById(request.getDoctorId());
        if (doctorOpt.isEmpty()) {
            throw new RuntimeException("Doctor not found");
        }

        // Kiểm tra thời gian hợp lệ (ví dụ: không đặt lịch trong quá khứ)
        if (request.getScheduleTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot book appointment in the past");
        }

        // Kiểm tra bác sĩ có rảnh không (giả sử mỗi lịch hẹn kéo dài 1 giờ)
        LocalDateTime start = request.getScheduleTime();
        LocalDateTime end = start.plusHours(1);
        List<Appointment> existingAppointments = appointmentRepository.findByDoctorIdAndScheduleTimeBetween(request.getDoctorId(), start, end);
        if (!existingAppointments.isEmpty()) {
            throw new RuntimeException("Doctor is not available at this time");
        }

        // Tạo lịch hẹn mới
        Appointment appointment = new Appointment();
        appointment.setPatient(patientOpt.get());
        appointment.setDoctor(doctorOpt.get());
        appointment.setScheduleTime(request.getScheduleTime());
        appointment.setStatus("PENDING");

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public Appointment updateAppointmentStatus(Long appointmentId, String status) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }
        Appointment appointment = appointmentOpt.get();
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }
}
