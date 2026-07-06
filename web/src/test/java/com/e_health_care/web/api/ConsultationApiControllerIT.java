package com.e_health_care.web.api;

import com.e_health_care.web.AbstractIntegrationTest;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Description;

@Epic("Doctor Management")
@Feature("Doctor Consultation API")
class ConsultationApiControllerIT extends AbstractIntegrationTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientClinicalInforRepository patientClinicalInforRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String doctorToken;
    private static Long patientId;

    @BeforeEach
    void setUp() {
        // Seed doctor
        Doctor doctor = doctorRepository.findByEmail("it_doctor@test.com");
        if (doctor == null) {
            Doctor d = new Doctor();
            d.setEmail("it_doctor@test.com");
            d.setPassword(passwordEncoder.encode("Password123"));
            d.setFirstName("IT");
            d.setLastName("Doctor");
            d.setField("General");
            d.setPhone("0909123457");
            d.setAddress("Test Address");
            d.setROLE("ROLE_DOCTOR");
            doctor = doctorRepository.save(d);
        }

        // Seed patient
        Patient patient = patientRepository.findByEmail("it_patient2@test.com").orElse(null);
        if (patient == null) {
            Patient p = new Patient();
            p.setEmail("it_patient2@test.com");
            p.setPassword(passwordEncoder.encode("Password123"));
            p.setFirstName("IT");
            p.setLastName("Patient2");
            p.setPhone("0909123458");
            p.setAddress("Test Address");
            patient = patientRepository.save(p);
        }
        patientId = patient.getId();

        // Login doctor to get token if not cached
        if (doctorToken == null) {
            HttpHeaders headers = jsonHeaders();
            Map<String, String> body = Map.of(
                    "email", "it_doctor@test.com",
                    "password", "Password123"
            );
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl() + "/api/doctor/login",
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            assertNotNull(response.getBody(), "Doctor login failed, response body is null");
            doctorToken = (String) response.getBody().get("token");
            assertNotNull(doctorToken, "Doctor login failed, token is null");
        }
    }

    // ══════ Patient Information API (Bước 3) ══════

    @Test
    @DisplayName("getPatientInfo_shouldReturn200_whenDoctorAuthenticated")
    @Story("Xem thông tin bệnh nhân khi bác sĩ đã xác thực")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra API trả về HTTP 200 kèm thông tin patient và clinicalInfo khi bác sĩ đã đăng nhập hợp lệ.")
    void getPatientInfo_shouldReturn200_whenDoctorAuthenticated() {
        HttpHeaders headers = jsonWithCookie("jwt-doctor-token", doctorToken);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/doctor/patient/" + patientId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("patient"));
        assertTrue(response.getBody().containsKey("clinicalInfo"));
    }

    @Test
    @DisplayName("getPatientInfo_shouldReturn401_whenNoToken")
    @Story("Xem thông tin bệnh nhân khi không có token")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra API trả về HTTP 401 hoặc 403 khi request không có token xác thực.")
    void getPatientInfo_shouldReturn401_whenNoToken() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/api/doctor/patient/" + patientId,
                Map.class
        );
        // Requirement specifies "expect HTTP 401 hoặc 403"
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED || response.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    // ══════ Update Clinical Information API (Bước 4) ══════

    @Test
    @DisplayName("updatePatientClinical_shouldReturn200_whenValid")
    @Story("Cập nhật bệnh án lâm sàng thành công")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra API cập nhật bệnh án trả về HTTP 200 kèm thông báo thành công khi dữ liệu hợp lệ.")
    void updatePatientClinical_shouldReturn200_whenValid() {
        HttpHeaders headers = jsonWithCookie("jwt-doctor-token", doctorToken);
        Map<String, String> body = Map.of(
                "bloodType", "O+",
                "allergies", "Peanuts",
                "chronicDiseases", "Asthma",
                "familyMedicalHistory", "None"
        );
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/doctor/patient/" + patientId,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cập nhật bệnh án thành công", response.getBody().get("message"));
    }

    @Test
    @DisplayName("updatePatientClinical_shouldReturn400_whenPatientNotFound")
    @Story("Cập nhật bệnh án cho bệnh nhân không tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra API trả về HTTP 400 kèm thông báo lỗi khi patientId không tồn tại trong hệ thống.")
    void updatePatientClinical_shouldReturn400_whenPatientNotFound() {
        HttpHeaders headers = jsonWithCookie("jwt-doctor-token", doctorToken);
        Map<String, String> body = Map.of(
                "bloodType", "AB-",
                "allergies", "Dust",
                "chronicDiseases", "Hypertension",
                "familyMedicalHistory", "Diabetes"
        );
        // Use a non-existent patient ID (e.g. 999999)
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/doctor/patient/999999",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
    }

    // ══════ Refresh Token API (Dashboard API) (Bước 5) ══════

    @Test
    @DisplayName("getDashboard_shouldReturn200_whenDoctorAuthenticated")
    @Story("Xem dashboard bác sĩ khi đã xác thực")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra API dashboard bác sĩ trả về HTTP 200 khi bác sĩ đã đăng nhập hợp lệ.")
    void getDashboard_shouldReturn200_whenDoctorAuthenticated() {
        HttpHeaders headers = jsonWithCookie("jwt-doctor-token", doctorToken);
        ResponseEntity<Object[]> response = restTemplate.exchange(
                baseUrl() + "/api/doctor/dashboard",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object[].class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("getDashboard_shouldReturn401_whenNoToken")
    @Story("Xem dashboard bác sĩ khi không có token")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra API dashboard bác sĩ trả về HTTP 401 khi request không có token xác thực.")
    void getDashboard_shouldReturn401_whenNoToken() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/api/doctor/dashboard",
                Map.class
        );
        // Requirement specifies "expect HTTP 401"
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}