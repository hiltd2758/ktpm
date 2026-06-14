package com.e_health_care.web.api;

import com.e_health_care.web.AbstractIntegrationTest;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConsultationApiControllerIT extends AbstractIntegrationTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String doctorToken;
    private static Long patientId;

    @BeforeEach
    void setUp() {
        // Seed doctor
        if (doctorRepository.findByEmail("it_doctor@test.com") == null) {
            Doctor d = new Doctor();
            d.setEmail("it_doctor@test.com");
            d.setPassword(passwordEncoder.encode("Password123"));
            d.setFirstName("IT");
            d.setLastName("Doctor");
            d.setField("General");
            d.setPhone("0909123457");
            d.setAddress("Test Address");
            d.setROLE("ROLE_DOCTOR");
            doctorRepository.save(d);
        }

        // Seed patient
        if (patientRepository.findByEmail("it_patient2@test.com").isEmpty()) {
            Patient p = new Patient();
            p.setEmail("it_patient2@test.com");
            p.setPassword(passwordEncoder.encode("Password123"));
            p.setFirstName("IT");
            p.setLastName("Patient2");
            p.setPhone("0909123458");
            p.setAddress("Test Address");
//            p.setROLE("ROLE_PATIENT");
            Patient saved = patientRepository.save(p);
            patientId = saved.getId();
        } else {
            patientId = patientRepository.findByEmail("it_patient2@test.com")
                    .get().getId();
        }

        // Login doctor
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
            doctorToken = (String) response.getBody().get("token");
        }
    }

    // ══════ GET /api/doctor/dashboard ══════

    @Test
    @Order(1)
    @DisplayName("IT_CONSULT_01 — getDashboard: không có token → 401")
    void getDashboard_noToken_shouldReturn401() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/api/doctor/dashboard",
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(2)
    @DisplayName("IT_CONSULT_02 — getDashboard: có token → 200 + list patients")
    void getDashboard_withToken_shouldReturn200() {
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

    // ══════ GET /api/doctor/patient/{id} ══════

    @Test
    @Order(3)
    @DisplayName("IT_CONSULT_03 — getPatientRecord: không có token → 401")
    void getPatientRecord_noToken_shouldReturn401() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/api/doctor/patient/" + patientId,
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(4)
    @DisplayName("IT_CONSULT_04 — getPatientRecord: có token, patient tồn tại → 200")
    void getPatientRecord_withToken_patientExists_shouldReturn200() {
        HttpHeaders headers = jsonWithCookie("jwt-doctor-token", doctorToken);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/doctor/patient/" + patientId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().containsKey("patient"));
        assertTrue(response.getBody().containsKey("clinicalInfo"));
    }

    @Test
    @Order(5)
    @DisplayName("IT_CONSULT_05 — getPatientRecord: patient không tồn tại → 404")
    void getPatientRecord_patientNotFound_shouldReturn404() {
        HttpHeaders headers = jsonWithCookie("jwt-doctor-token", doctorToken);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/doctor/patient/99999",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ══════ POST /api/doctor/patient/{id} ══════

    @Test
    @Order(6)
    @DisplayName("IT_CONSULT_06 — updatePatientRecord: không có token → 401")
    void updatePatientRecord_noToken_shouldReturn401() {
        Map<String, String> body = Map.of("bloodType", "B+");
        HttpHeaders headers = jsonHeaders();
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/doctor/patient/" + patientId,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("IT_CONSULT_07 — updatePatientRecord: có token, hợp lệ → 200")
    void updatePatientRecord_withToken_shouldReturn200() {
        HttpHeaders headers = jsonWithCookie("jwt-doctor-token", doctorToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of(
                "bloodType", "O+",
                "allergies", "None",
                "chronicDiseases", "None",
                "familyMedicalHistory", "None"
        );
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/doctor/patient/" + patientId,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cập nhật bệnh án thành công", response.getBody().get("message"));
    }
}