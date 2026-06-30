package com.e_health_care.web.patient.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.model.Appointment;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import com.e_health_care.web.patient.dto.AppointmentRequestDTO;
import com.e_health_care.web.patient.repository.PatientAppointmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Fix: thêm import Allure annotations để gắn nhãn nghiệp vụ cho report
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Description;

@ExtendWith(MockitoExtension.class)
// Fix: @Epic/@Feature dùng để nhóm test theo nghiệp vụ, hiển thị ở tab "Behaviors"
// trong Allure report thay vì chỉ hiển thị theo tên class kỹ thuật
@Epic("Patient Management")
@Feature("Appointment Booking")
public class PatientAppointmentServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientAppointmentRepository appointmentRepository;

    @InjectMocks
    private PatientAppointmentService appointmentService;

    private Patient mockPatient;
    private Doctor mockDoctor;
    private Appointment mockAppointment;

    @BeforeEach
    void setUp() {
        mockPatient = new Patient();
        mockPatient.setId(1L);

        mockDoctor = new Doctor();
        mockDoctor.setId(2L);

        mockAppointment = new Appointment();
        mockAppointment.setId(10L);
        mockAppointment.setPatient(mockPatient);
        mockAppointment.setDoctor(mockDoctor);
        mockAppointment.setScheduleTime(LocalDateTime.now().plusDays(1));
        mockAppointment.setStatus("PENDING");
    }

    // ==========================================
    // CÁC TEST CASE CHO HÀM bookAppointment()
    // ==========================================

    @Test
    @Story("Patient not found")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Đặt lịch hẹn phải bị từ chối nếu patientId không tồn tại trong hệ thống")
    void bookAppointment_PatientNotFound_ThrowsException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(99L);
        request.setDoctorId(2L);
        request.setScheduleTime(LocalDateTime.now().plusDays(1));

        assertThrows(RuntimeException.class, () -> appointmentService.bookAppointment(request),
                "Nên ném lỗi khi không tìm thấy Bệnh nhân");
    }

    @Test
    @Story("Doctor not found")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Đặt lịch hẹn phải bị từ chối nếu doctorId không tồn tại trong hệ thống")
    void bookAppointment_DoctorNotFound_ThrowsException() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(1L);
        request.setDoctorId(99L);
        request.setScheduleTime(LocalDateTime.now().plusDays(1));

        assertThrows(RuntimeException.class, () -> appointmentService.bookAppointment(request),
                "Nên ném lỗi khi không tìm thấy Bác sĩ");
    }

    @Test
    @Story("Invalid schedule time")
    @Severity(SeverityLevel.NORMAL)
    @Description("Không cho phép đặt lịch hẹn với thời gian nằm trong quá khứ")
    void bookAppointment_TimeInPast_ThrowsException() {
        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setScheduleTime(LocalDateTime.now().minusDays(1)); // Quá khứ

        assertThrows(RuntimeException.class, () -> appointmentService.bookAppointment(request),
                "Nên ném lỗi khi lịch hẹn nằm trong quá khứ");
    }

    @Test
    @Story("Doctor schedule conflict")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Không cho phép đặt lịch trùng khung giờ Bác sĩ đã có lịch hẹn khác")
    void bookAppointment_DoctorAlreadyBooked_ThrowsException() {
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = scheduleTime.plusHours(1);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(mockDoctor));

        when(appointmentRepository.findByDoctorIdAndScheduleTimeBetween(2L, scheduleTime, endTime))
                .thenReturn(Arrays.asList(mockAppointment));

        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setScheduleTime(scheduleTime);

        assertThrows(RuntimeException.class, () -> appointmentService.bookAppointment(request),
                "Nên ném lỗi khi Bác sĩ đã kẹt lịch");
    }

    @Test
    @Story("Successful booking")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Đặt lịch hẹn thành công khi dữ liệu hợp lệ, lưu Appointment vào repository")
    void bookAppointment_ValidData_Success() {
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);

        // ĐÃ SỬA CHỖ NÀY: Dùng any() để nới lỏng kiểm tra, Mockito sẽ không bắt bẻ tham số nữa
        when(patientRepository.findById(any())).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findById(any())).thenReturn(Optional.of(mockDoctor));
        when(appointmentRepository.findByDoctorIdAndScheduleTimeBetween(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setScheduleTime(scheduleTime);

        appointmentService.bookAppointment(request);

        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    // TEST CASE CHO HÀM getAppointmentsByPatient()

    @Test
    @Story("View patient appointments")
    @Severity(SeverityLevel.NORMAL)
    @Description("Lấy đúng danh sách lịch hẹn của một bệnh nhân theo patientId")
    void getAppointmentsByPatient_Success() {
        List<Appointment> mockList = Arrays.asList(mockAppointment);
        when(appointmentRepository.findByPatientId(1L)).thenReturn(mockList);

        List<Appointment> result = appointmentService.getAppointmentsByPatient(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    // TEST CASE CHO HÀM updateStatus()
    @Test
    @Story("Update appointment status")
    @Severity(SeverityLevel.MINOR)
    @Description("Ném lỗi khi cập nhật trạng thái cho lịch hẹn không tồn tại")
    void updateStatus_AppointmentNotFound_ThrowsException() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> appointmentService.updateAppointmentStatus(99L, "CONFIRMED"));
    }

    @Test
    @Story("Update appointment status")
    @Severity(SeverityLevel.NORMAL)
    @Description("Cập nhật trạng thái lịch hẹn thành công và lưu lại vào repository")
    void updateStatus_Success() {
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(mockAppointment));

        appointmentService.updateAppointmentStatus(10L, "CONFIRMED");

        assertEquals("CONFIRMED", mockAppointment.getStatus());
        verify(appointmentRepository, times(1)).save(mockAppointment);
    }
}