package com.e_health_care.web.patient.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.patient.model.Appointment;
import com.e_health_care.web.patient.repository.PatientAppointmentRepository;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * State Transition Testing — Appointment.status
 *
 * State diagram (thiết kế đúng theo nghiệp vụ):
 *   PENDING    -> CONFIRMED   (valid)
 *   PENDING    -> CANCELLED   (valid)
 *   CONFIRMED  -> COMPLETED   (valid)
 *   CONFIRMED  -> CANCELLED   (valid)
 *   CANCELLED  -> (terminal, không thể chuyển tiếp)
 *   COMPLETED  -> (terminal, không thể chuyển tiếp)
 *
 * Ghi chú: updateAppointmentStatus() hiện tại KHÔNG validate transition,
 * nên các test "invalid transition" sẽ FAIL theo kỳ vọng nghiệp vụ đúng,
 * nhưng PASS theo hành vi thực tế của code — đây là DEFECT cần báo cáo.
 */
// Fix: @Epic/@Feature dùng để nhóm test theo nghiệp vụ ở tab "Behaviors"
@Epic("Patient Management")
@Feature("Appointment Status Management")
class PatientAppointmentServiceStateTransitionTest extends BaseServiceTest {

    @Mock
    private PatientAppointmentRepository appointmentRepository;

    @InjectMocks
    private PatientAppointmentService service;

    private Appointment appointmentWithStatus(String status) {
        Appointment a = new Appointment();
        a.setId(1L);
        a.setStatus(status);
        return a;
    }

    // ══════ VALID TRANSITIONS ══════

    @Test
    @DisplayName("STT-01: PENDING -> CONFIRMED (valid)")
    @Story("Chuyển trạng thái lịch hẹn từ PENDING sang CONFIRMED")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra hệ thống cập nhật thành công trạng thái lịch hẹn từ PENDING sang CONFIRMED, đây là chuyển trạng thái hợp lệ trong luồng nghiệp vụ chính khi bác sĩ xác nhận lịch hẹn.")
    void pending_to_confirmed_shouldSucceed() {
        Appointment existing = appointmentWithStatus("PENDING");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = service.updateAppointmentStatus(1L, "CONFIRMED");

        assertEquals("CONFIRMED", result.getStatus());
    }

    @Test
    @DisplayName("STT-02: PENDING -> CANCELLED (valid)")
    @Story("Chuyển trạng thái lịch hẹn từ PENDING sang CANCELLED")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra hệ thống cập nhật thành công trạng thái lịch hẹn từ PENDING sang CANCELLED, đây là chuyển trạng thái hợp lệ khi bệnh nhân hoặc bác sĩ hủy lịch chờ xác nhận.")
    void pending_to_cancelled_shouldSucceed() {
        Appointment existing = appointmentWithStatus("PENDING");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = service.updateAppointmentStatus(1L, "CANCELLED");

        assertEquals("CANCELLED", result.getStatus());
    }

    @Test
    @DisplayName("STT-03: CONFIRMED -> COMPLETED (valid)")
    @Story("Chuyển trạng thái lịch hẹn từ CONFIRMED sang COMPLETED")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra hệ thống cập nhật thành công trạng thái lịch hẹn từ CONFIRMED sang COMPLETED, đây là chuyển trạng thái hợp lệ khi buổi khám đã hoàn thành.")
    void confirmed_to_completed_shouldSucceed() {
        Appointment existing = appointmentWithStatus("CONFIRMED");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = service.updateAppointmentStatus(1L, "COMPLETED");

        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    @DisplayName("STT-04: CONFIRMED -> CANCELLED (valid)")
    @Story("Chuyển trạng thái lịch hẹn từ CONFIRMED sang CANCELLED")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống cập nhật thành công trạng thái lịch hẹn từ CONFIRMED sang CANCELLED, đây là chuyển trạng thái hợp lệ khi hủy lịch đã được xác nhận trước ngày khám.")
    void confirmed_to_cancelled_shouldSucceed() {
        Appointment existing = appointmentWithStatus("CONFIRMED");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = service.updateAppointmentStatus(1L, "CANCELLED");

        assertEquals("CANCELLED", result.getStatus());
    }

    // ══════ INVALID TRANSITIONS — DEFECT ĐÃ ĐƯỢC FIX ══════
    // Cập nhật 30/06/2026: Dev đã thêm validate terminal state.
    // Service hiện throw IllegalStateException khi cố chuyển từ CANCELLED/COMPLETED.
    // Test được cập nhật lại để khớp hành vi đúng nghiệp vụ — Retest PASS.

    @Test
    @DisplayName("STT-05 [DEFECT-01 - RESOLVED]: CANCELLED -> PENDING -> throw IllegalStateException")
    @Story("Ngăn chuyển trạng thái từ trạng thái cuối CANCELLED sang trạng thái khác")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném IllegalStateException với thông báo 'Cannot change status from terminal state: CANCELLED' khi cố gắng chuyển lịch hẹn đã bị hủy (terminal state) sang bất kỳ trạng thái nào khác. DEFECT-01 đã được fix ngày 30/06/2026.")
    void cancelled_to_pending_shouldThrow() {
        Appointment existing = appointmentWithStatus("CANCELLED");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.updateAppointmentStatus(1L, "PENDING"));

        assertEquals("Cannot change status from terminal state: CANCELLED", ex.getMessage());
    }

    @Test
    @DisplayName("STT-06 [DEFECT-02 - RESOLVED]: COMPLETED -> PENDING -> throw IllegalStateException")
    @Story("Ngăn chuyển trạng thái từ trạng thái cuối COMPLETED sang trạng thái khác")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném IllegalStateException với thông báo 'Cannot change status from terminal state: COMPLETED' khi cố gắng chuyển lịch hẹn đã hoàn thành (terminal state) sang PENDING. DEFECT-02 đã được fix ngày 30/06/2026.")
    void completed_to_pending_shouldThrow() {
        Appointment existing = appointmentWithStatus("COMPLETED");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.updateAppointmentStatus(1L, "PENDING"));

        assertEquals("Cannot change status from terminal state: COMPLETED", ex.getMessage());
    }

    @Test
    @DisplayName("STT-07 [DEFECT-03 - RESOLVED]: COMPLETED -> CONFIRMED -> throw IllegalStateException")
    @Story("Ngăn chuyển trạng thái từ trạng thái cuối COMPLETED sang trạng thái khác")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném IllegalStateException với thông báo 'Cannot change status from terminal state: COMPLETED' khi cố gắng chuyển lịch hẹn đã hoàn thành (terminal state) sang CONFIRMED. DEFECT-03 đã được fix ngày 30/06/2026.")
    void completed_to_confirmed_shouldThrow() {
        Appointment existing = appointmentWithStatus("COMPLETED");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.updateAppointmentStatus(1L, "CONFIRMED"));

        assertEquals("Cannot change status from terminal state: COMPLETED", ex.getMessage());
    }

    // ══════ EDGE CASE ══════

    @Test
    @DisplayName("STT-08: updateAppointmentStatus với appointmentId không tồn tại -> throw")
    @Story("Cập nhật trạng thái lịch hẹn với ID không tồn tại")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống ném RuntimeException khi cố gắng cập nhật trạng thái của lịch hẹn có ID không tồn tại trong cơ sở dữ liệu.")
    void updateStatus_appointmentNotFound_shouldThrow() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.updateAppointmentStatus(99L, "CONFIRMED"));
    }
}