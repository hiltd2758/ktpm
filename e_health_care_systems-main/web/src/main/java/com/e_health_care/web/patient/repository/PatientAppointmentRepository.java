package com.e_health_care.web.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.e_health_care.web.patient.model.Appointment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PatientAppointmentRepository extends JpaRepository<Appointment, Long> {
    // Tìm các lịch hẹn của bác sĩ trong khoảng thời gian
    List<Appointment> findByDoctorIdAndScheduleTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
    
    // Tìm các lịch hẹn của bệnh nhân
    List<Appointment> findByPatientId(Long patientId);

    // 🔹 Lấy tất cả lịch hẹn theo bác sĩ
    List<Appointment> findByDoctorId(Long doctorId);

    // 🔹 Lấy lịch hẹn theo trạng thái
    List<Appointment> findByStatus(String status);
}
