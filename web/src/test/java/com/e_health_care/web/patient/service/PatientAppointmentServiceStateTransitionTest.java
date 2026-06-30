package com.e_health_care.web.patient.service;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.patient.model.Appointment;
import com.e_health_care.web.patient.repository.PatientAppointmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    void pending_to_confirmed_shouldSucceed() {
        Appointment existing = appointmentWithStatus("PENDING");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = service.updateAppointmentStatus(1L, "CONFIRMED");

        assertEquals("CONFIRMED", result.getStatus());
    }

    @Test
    @DisplayName("STT-02: PENDING -> CANCELLED (valid)")
    void pending_to_cancelled_shouldSucceed() {
        Appointment existing = appointmentWithStatus("PENDING");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = service.updateAppointmentStatus(1L, "CANCELLED");

        assertEquals("CANCELLED", result.getStatus());
    }

    @Test
    @DisplayName("STT-03: CONFIRMED -> COMPLETED (valid)")
    void confirmed_to_completed_shouldSucceed() {
        Appointment existing = appointmentWithStatus("CONFIRMED");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = service.updateAppointmentStatus(1L, "COMPLETED");

        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    @DisplayName("STT-04: CONFIRMED -> CANCELLED (valid)")
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
    void cancelled_to_pending_shouldThrow() {
        Appointment existing = appointmentWithStatus("CANCELLED");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.updateAppointmentStatus(1L, "PENDING"));

        assertEquals("Cannot change status from terminal state: CANCELLED", ex.getMessage());
    }

    @Test
    @DisplayName("STT-06 [DEFECT-02 - RESOLVED]: COMPLETED -> PENDING -> throw IllegalStateException")
    void completed_to_pending_shouldThrow() {
        Appointment existing = appointmentWithStatus("COMPLETED");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.updateAppointmentStatus(1L, "PENDING"));

        assertEquals("Cannot change status from terminal state: COMPLETED", ex.getMessage());
    }

    @Test
    @DisplayName("STT-07 [DEFECT-03 - RESOLVED]: COMPLETED -> CONFIRMED -> throw IllegalStateException")
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
    void updateStatus_appointmentNotFound_shouldThrow() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.updateAppointmentStatus(99L, "CONFIRMED"));
    }
}