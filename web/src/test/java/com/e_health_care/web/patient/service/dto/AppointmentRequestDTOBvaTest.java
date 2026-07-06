package com.e_health_care.web.patient.service.dto;

import com.e_health_care.web.BvaValidationHelper;
import com.e_health_care.web.patient.dto.AppointmentRequestDTO;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Description;

@Epic("Patient Management")
@Feature("Appointment Request DTO Validation")
class AppointmentRequestDTOBvaTest {

    private AppointmentRequestDTO createValidDTO() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(1L);
        dto.setScheduleTime(LocalDateTime.now().plusDays(1));
        return dto;
    }

    @Test
    @Story("patientId null")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO không hợp lệ khi patientId là null.")
    void patientId_null_shouldBeInvalid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setPatientId(null);
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("patientId biên dưới (1)")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra DTO hợp lệ khi patientId ở giá trị biên dưới nhỏ nhất (1).")
    void patientId_min_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setPatientId(1L);
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("patientId giá trị thông thường")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO hợp lệ khi patientId là giá trị bình thường (500).")
    void patientId_nominal_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setPatientId(500L);
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("patientId biên trên (Long.MAX_VALUE)")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra DTO hợp lệ khi patientId ở giá trị biên trên lớn nhất (Long.MAX_VALUE).")
    void patientId_max_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setPatientId(Long.MAX_VALUE);
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("doctorId null")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO không hợp lệ khi doctorId là null.")
    void doctorId_null_shouldBeInvalid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setDoctorId(null);
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("doctorId biên dưới (1)")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra DTO hợp lệ khi doctorId ở giá trị biên dưới nhỏ nhất (1).")
    void doctorId_min_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setDoctorId(1L);
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("doctorId giá trị thông thường")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO hợp lệ khi doctorId là giá trị bình thường (500).")
    void doctorId_nominal_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setDoctorId(500L);
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("doctorId biên trên (Long.MAX_VALUE)")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra DTO hợp lệ khi doctorId ở giá trị biên trên lớn nhất (Long.MAX_VALUE).")
    void doctorId_max_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setDoctorId(Long.MAX_VALUE);
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("scheduleTime null")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra DTO không hợp lệ khi scheduleTime là null.")
    void scheduleTime_null_shouldBeInvalid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setScheduleTime(null);
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("scheduleTime trong quá khứ")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra DTO không hợp lệ khi scheduleTime nằm trong quá khứ.")
    void scheduleTime_past_shouldBeInvalid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setScheduleTime(LocalDateTime.now().minusDays(1));
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("scheduleTime tại thời điểm hiện tại")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO không hợp lệ khi scheduleTime bằng đúng thời điểm hiện tại (biên).")
    void scheduleTime_present_shouldBeInvalid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setScheduleTime(LocalDateTime.now());
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("scheduleTime gần tương lai")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra DTO hợp lệ khi scheduleTime cách hiện tại 5 phút (biên hợp lệ gần nhất).")
    void scheduleTime_nearFuture_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setScheduleTime(LocalDateTime.now().plusMinutes(5));
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("scheduleTime xa trong tương lai")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra DTO hợp lệ khi scheduleTime cách hiện tại 30 ngày.")
    void scheduleTime_farFuture_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setScheduleTime(LocalDateTime.now().plusDays(30));
        assertTrue(BvaValidationHelper.isValid(dto));
    }
}