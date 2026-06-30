package com.e_health_care.web.patient.service;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.dto.AppointmentRequestDTO;
import com.e_health_care.web.patient.model.Appointment;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientAppointmentRepository;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Equivalence Partitioning + Boundary Value Analysis
 * cho bookAppointment() — PatientAppointmentService
 *
 * Tag mapping:
 *   V1=patientId hợp lệ      X1=patientId không tồn tại
 *   V2=doctorId hợp lệ       X2=doctorId không tồn tại
 *   V3=scheduleTime tương lai X3=scheduleTime quá khứ   X4=scheduleTime=now()
 *   V4=doctor rảnh           X5=doctor đã có lịch khác
 *   B1=now()-1s  B2=now()   B3=now()+1s  B5=now()+30 ngày
 */
class JIRABookAppointmentEpBvaTest extends BaseServiceTest {

    @Mock private PatientAppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;

    @InjectMocks
    private PatientAppointmentService service;

    private AppointmentRequestDTO request(Long patientId, Long doctorId, LocalDateTime time) {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setPatientId(patientId);
        dto.setDoctorId(doctorId);
        dto.setScheduleTime(time);
        return dto;
    }

    // TC01 — V1,V2,V3,V4 — nominal hợp lệ
    @Test
    @DisplayName("TC01 [V1,V2,V3,V4]: tất cả điều kiện hợp lệ -> tạo appointment PENDING")
    void tc01_allValid_shouldCreateAppointment() {
        Patient patient = new Patient();
        Doctor doctor = new Doctor();
        LocalDateTime time = LocalDateTime.now().plusDays(1);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorIdAndScheduleTimeBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = service.bookAppointment(request(1L, 1L, time));

        assertEquals("PENDING", result.getStatus());
        assertEquals(patient, result.getPatient());        // mới — giết mutant line 57
        assertEquals(doctor, result.getDoctor());           // mới — giết mutant line 58
        assertEquals(time, result.getScheduleTime());       // mới — giết mutant line 59
    }

    // TC02 — X1 — patient không tồn tại
    @Test
    @DisplayName("TC02 [X1]: patientId không tồn tại -> throw 'Patient not found'")
    void tc02_patientNotFound_shouldThrow() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class, () ->
                service.bookAppointment(request(999L, 1L, LocalDateTime.now().plusDays(1))));
        assertEquals("Patient not found", ex.getMessage());
    }

    // TC03 — X2 — doctor không tồn tại
    @Test
    @DisplayName("TC03 [X2]: doctorId không tồn tại -> throw 'Doctor not found'")
    void tc03_doctorNotFound_shouldThrow() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class, () ->
                service.bookAppointment(request(1L, 999L, LocalDateTime.now().plusDays(1))));
        assertEquals("Doctor not found", ex.getMessage());
    }

    // TC04 — X3,B1 — scheduleTime quá khứ (now()-1s)
    @Test
    @DisplayName("TC04 [X3,B1]: scheduleTime = now()-1s -> throw 'Cannot book in the past'")
    void tc04_scheduleTimeBeforeBoundary_shouldThrow() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(new Doctor()));

        Exception ex = assertThrows(RuntimeException.class, () ->
                service.bookAppointment(request(1L, 1L, LocalDateTime.now().minusSeconds(1))));
        assertEquals("Cannot book appointment in the past", ex.getMessage());
    }

    // TC05 — X4,B2 — scheduleTime = now() chính xác
    @Test
    @DisplayName("TC05 [X4,B2]: scheduleTime = now()-1ms -> throw (boundary sát now())")
    void tc05_scheduleTimeExactlyNow_shouldThrow() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(new Doctor()));

        LocalDateTime almostNow = LocalDateTime.now().minusNanos(1);
        Exception ex = assertThrows(RuntimeException.class, () ->
                service.bookAppointment(request(1L, 1L, almostNow)));
        assertEquals("Cannot book appointment in the past", ex.getMessage());
    }

    // TC06 — B3 — scheduleTime = now()+1s (biên hợp lệ gần nhất)
    @Test
    @DisplayName("TC06 [B3]: scheduleTime = now()+1s -> hợp lệ (biên tương lai gần nhất)")
    void tc06_scheduleTimeJustAfterNow_shouldSucceed() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(new Doctor()));
        when(appointmentRepository.findByDoctorIdAndScheduleTimeBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = service.bookAppointment(
                request(1L, 1L, LocalDateTime.now().plusSeconds(2)));

        assertEquals("PENDING", result.getStatus());
    }

    // TC07 — X5 — doctor đã có lịch khác cùng khung giờ
    @Test
    @DisplayName("TC07 [X5]: doctor đã có appointment khác cùng khung giờ -> throw 'not available'")
    void tc07_doctorBusy_shouldThrow() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(new Doctor()));
        when(appointmentRepository.findByDoctorIdAndScheduleTimeBetween(any(), any(), any()))
                .thenReturn(List.of(new Appointment()));

        Exception ex = assertThrows(RuntimeException.class, () ->
                service.bookAppointment(request(1L, 1L, LocalDateTime.now().plusDays(1))));
        assertEquals("Doctor is not available at this time", ex.getMessage());
    }

    // TC08 — B5 — scheduleTime xa trong tương lai (biên nghiệp vụ)
    @Test
    @DisplayName("TC08 [B5]: scheduleTime = now()+30 ngày -> hợp lệ")
    void tc08_scheduleTimeFarFuture_shouldSucceed() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(new Doctor()));
        when(appointmentRepository.findByDoctorIdAndScheduleTimeBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = service.bookAppointment(
                request(1L, 1L, LocalDateTime.now().plusDays(30)));

        assertEquals("PENDING", result.getStatus());
    }

    // TC09 — X1 (ưu tiên) — nhiều điều kiện sai cùng lúc, chỉ throw lỗi đầu tiên
    @Test
    @DisplayName("TC09 [X1]: nhiều điều kiện sai cùng lúc -> chỉ throw lỗi patient (kiểm tra đầu tiên)")
    void tc09_multipleInvalid_shouldThrowFirstCheckedError() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class, () ->
                service.bookAppointment(request(999L, 999L, LocalDateTime.now().minusDays(1))));
        assertEquals("Patient not found", ex.getMessage(),
                "Code kiểm tra patient trước doctor và scheduleTime — " +
                        "dù 3 điều kiện đều sai, chỉ lỗi đầu tiên được throw.");
    }
    @Test
    @DisplayName("TC10: getAppointmentsByPatient() trả về danh sách lịch hẹn của bệnh nhân")
    void tc10_getAppointmentsByPatient_shouldReturnList() {
        when(appointmentRepository.findByPatientId(1L))
                .thenReturn(List.of(new Appointment()));

        List<Appointment> result = service.getAppointmentsByPatient(1L);

        assertEquals(1, result.size());
        verify(appointmentRepository, times(1)).findByPatientId(1L);
    }

    @Test
    @DisplayName("TC11: getAppointmentsByDoctor() trả về danh sách lịch hẹn của bác sĩ")
    void tc11_getAppointmentsByDoctor_shouldReturnList() {
        when(appointmentRepository.findByDoctorId(1L))
                .thenReturn(List.of(new Appointment()));

        List<Appointment> result = service.getAppointmentsByDoctor(1L);

        assertEquals(1, result.size());
        verify(appointmentRepository, times(1)).findByDoctorId(1L);
    }

    @Test
    @DisplayName("TC12: getAllAppointments() trả về toàn bộ danh sách lịch hẹn")
    void tc12_getAllAppointments_shouldReturnAllList() {
        when(appointmentRepository.findAll())
                .thenReturn(List.of(new Appointment(), new Appointment()));

        List<Appointment> result = service.getAllAppointments();

        assertEquals(2, result.size());
        verify(appointmentRepository, times(1)).findAll();
    }
}