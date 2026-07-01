package com.e_health_care.web.admin.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;
import com.e_health_care.web.patient.repository.PatientRepository;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

// Fix: @Epic/@Feature dùng để nhóm test theo nghiệp vụ ở tab "Behaviors"
@Epic("Admin Management")
@Feature("Admin Patient Management")
class AdminManagementServiceTest extends BaseServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientClinicalInforRepository patientClinicalInforRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Query nativeQuery;

    @InjectMocks
    private AdminManagementService adminManagementService;

    // --- Part 1: createPatient Tests ---

    @Test
    @Story("Tạo tài khoản bệnh nhân với email đã tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném RuntimeException và không gọi hàm save khi admin cố tạo tài khoản bệnh nhân với email đã được đăng ký trước đó trong cơ sở dữ liệu.")
    void createPatient_emailAlreadyExists_throwsRuntimeException() {
        Patient patient = new Patient();
        patient.setEmail("existing@example.com");

        when(patientRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new Patient()));

        assertThrows(RuntimeException.class, () -> {
            adminManagementService.createPatient(patient);
        });

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    @Story("Tạo tài khoản bệnh nhân mới thành công")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra admin tạo thành công tài khoản bệnh nhân mới khi email chưa tồn tại, hệ thống gọi đúng một lần hàm save và trả về đối tượng Patient không null.")
    void createPatient_newEmail_callsSave() {
        Patient patient = new Patient();
        patient.setEmail("new@example.com");

        when(patientRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(patientRepository.save(patient)).thenReturn(patient);

        Patient result = adminManagementService.createPatient(patient);

        assertNotNull(result);
        verify(patientRepository, times(1)).save(patient);
    }

    // --- Part 2: updatePatient Tests ---

    @Test
    @Story("Cập nhật thông tin bệnh nhân với ID không tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném RuntimeException và không gọi hàm save khi admin cố cập nhật thông tin bệnh nhân với ID không tồn tại trong cơ sở dữ liệu.")
    void updatePatient_patientIdDoesNotExist_throwsRuntimeException() {
        Patient patient = new Patient();
        patient.setId(99L);

        when(patientRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            adminManagementService.updatePatient(patient);
        });

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    @Story("Cập nhật thông tin bệnh nhân thành công")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra admin cập nhật thành công thông tin bệnh nhân khi ID tồn tại, hệ thống gọi đúng một lần hàm save và trả về đối tượng Patient đã cập nhật.")
    void updatePatient_patientIdExists_callsSave() {
        Patient patient = new Patient();
        patient.setId(1L);

        when(patientRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.save(patient)).thenReturn(patient);

        Patient result = adminManagementService.updatePatient(patient);

        assertNotNull(result);
        verify(patientRepository, times(1)).save(patient);
    }

    // --- Part 3: deletePatient Tests ---

    @Test
    @Story("Xóa tài khoản bệnh nhân với ID không tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném RuntimeException và không gọi hàm deleteById khi admin cố xóa tài khoản bệnh nhân với ID không tồn tại trong cơ sở dữ liệu.")
    void deletePatient_patientIdDoesNotExist_throwsRuntimeException() {
        long id = 99L;

        when(patientRepository.existsById(id)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            adminManagementService.deletePatient(id);
        });

        verify(patientRepository, never()).deleteById(anyLong());
    }

    @Test
    @Story("Xóa tài khoản bệnh nhân thành công")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra admin xóa thành công tài khoản bệnh nhân khi ID tồn tại, hệ thống gọi đúng hàm deleteById và flush EntityManager để đảm bảo dữ liệu liên quan được dọn sạch.")
    void deletePatient_patientIdExists_callsDelete() {
        long id = 1L;

        when(patientRepository.existsById(id)).thenReturn(true);
        when(patientClinicalInforRepository.findByPatientId(id)).thenReturn(Optional.empty());
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);

        adminManagementService.deletePatient(id);

        verify(patientRepository, times(1)).deleteById(id);
        verify(entityManager, times(1)).flush();
    }
}