package com.e_health_care.web.api;

import com.e_health_care.web.AbstractIntegrationTest;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.model.PatientClinicalInfor;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;
import com.e_health_care.web.patient.repository.PatientRepository;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("Patient Management")
@Feature("Patient Clinical Info API")
class PatientClinicalInforControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientClinicalInforRepository patientClinicalInforRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static Long patientId;
    private static String patientToken;

    @BeforeEach
    void setUp() {
        // Tạo patient test nếu chưa có
        if (patientRepository.findByEmail("it_patient@test.com").isEmpty()) {
            Patient p = new Patient();
            p.setEmail("it_patient@test.com");
            p.setPassword(passwordEncoder.encode("Password123"));
            p.setFirstName("IT");
            p.setLastName("Patient");
            p.setPhone("0909123456");
            p.setAddress("Test Address");
//            p.setROLE("ROLE_PATIENT");
            Patient saved = patientRepository.save(p);
            patientId = saved.getId();
        } else {
            patientId = patientRepository.findByEmail("it_patient@test.com")
                    .get().getId();
        }

        // Login để lấy token
        if (patientToken == null) {
            HttpHeaders headers = jsonHeaders();
            Map<String, String> body = Map.of(
                    "email", "it_patient@test.com",
                    "password", "Password123"
            );
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl() + "/api/patient/login",
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            patientToken = (String) response.getBody().get("token");
        }
    }

    // ══════ GET /api/patient/clinical-info/{id} ══════

    @Test
    @Order(1)
    @DisplayName("IT_CLINICAL_01 — getClinicalInfo: không có token → 401")
    @Story("Xem thông tin lâm sàng khi không có token")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra API trả về HTTP 401 khi request không có token xác thực.")
    void getClinicalInfo_noToken_shouldReturn401() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/api/patient/clinical-info/" + patientId,
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(2)
    @DisplayName("IT_CLINICAL_02 — getClinicalInfo: có token, chưa có record → 200 với DTO rỗng")
    @Story("Xem thông tin lâm sàng khi chưa có dữ liệu")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra API trả về HTTP 200 kèm DTO rỗng khi bệnh nhân đã xác thực nhưng chưa có bản ghi thông tin lâm sàng.")
    void getClinicalInfo_noRecord_shouldReturn200WithEmptyDTO() {
        HttpHeaders headers = jsonWithCookie("jwt-patient-token", patientToken);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/patient/clinical-info/" + patientId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(3)
    @DisplayName("IT_CLINICAL_03 — getClinicalInfo: có token, có record → 200 với data")
    @Story("Xem thông tin lâm sàng khi đã có dữ liệu")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra API trả về HTTP 200 kèm đúng dữ liệu thông tin lâm sàng (nhóm máu) khi bệnh nhân đã có bản ghi.")
    void getClinicalInfo_withRecord_shouldReturn200WithData() {
        // Seed clinical info
        Patient p = patientRepository.findByEmail("it_patient@test.com").get();
        PatientClinicalInfor info = new PatientClinicalInfor();
        info.setPatient(p);
        info.setBloodType("A+");
        info.setAllergies("None");
        info.setChronicDiseases("None");
        info.setFamilyMedicalHistory("None");
        patientClinicalInforRepository.save(info);

        HttpHeaders headers = jsonWithCookie("jwt-patient-token", patientToken);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/patient/clinical-info/" + patientId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("A+", response.getBody().get("bloodType"));
    }
}