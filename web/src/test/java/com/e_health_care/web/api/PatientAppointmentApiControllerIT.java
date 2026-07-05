package com.e_health_care.web.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import com.e_health_care.web.AbstractIntegrationTest;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.dto.AppointmentRequestDTO;
import com.e_health_care.web.patient.model.Appointment;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientAppointmentRepository;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;
import com.e_health_care.web.patient.repository.PatientRepository;
import com.e_health_care.web.patient.service.PatientJwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PatientAppointmentApiControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientAppointmentRepository appointmentRepository;

    @Autowired
    private PatientClinicalInforRepository patientClinicalInforRepository;

    @Autowired
    private PatientJwtService jwtService;

    private Patient savedPatient;
    private Doctor savedDoctor;
    private String validToken;

    @BeforeEach
    void setUp() {
        // Xóa theo đúng thứ tự con -> cha để tránh vi phạm khóa ngoại (FK).
        // appointment và patient_clinical_info đều tham chiếu tới patient,
        // nên phải xóa hết trước khi xóa patient.
        appointmentRepository.deleteAll();
        patientClinicalInforRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();

        Patient patient = new Patient();
        patient.setEmail("patient_test@test.com");
        savedPatient = patientRepository.save(patient);

        Doctor doctor = new Doctor();
        savedDoctor = doctorRepository.save(doctor);

        validToken = jwtService.generateToken(savedPatient.getEmail());
    }

    // =========================================================================
    // BƯỚC 3 – APPOINTMENT BOOKING API (POST /api/patient/appointment/book)
    // =========================================================================

    @Test
    void bookAppointment_shouldReturn200_whenValid() {
        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(savedPatient.getId());
        request.setDoctorId(savedDoctor.getId());
        request.setScheduleTime(LocalDateTime.now().plusDays(2));

        HttpHeaders headers = jsonWithCookie("jwt-patient-token", validToken);
        HttpEntity<AppointmentRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/api/patient/appointment/book",
                entity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void bookAppointment_shouldReturn400_whenDoctorNotFound() {
        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(savedPatient.getId());
        request.setDoctorId(9999L);
        request.setScheduleTime(LocalDateTime.now().plusDays(2));

        HttpHeaders headers = jsonWithCookie("jwt-patient-token", validToken);
        HttpEntity<AppointmentRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/api/patient/appointment/book",
                entity,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void bookAppointment_shouldReturn401_whenNoToken() {
        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(savedPatient.getId());
        request.setDoctorId(savedDoctor.getId());
        request.setScheduleTime(LocalDateTime.now().plusDays(2));

        HttpHeaders headers = jsonHeaders();
        HttpEntity<AppointmentRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/api/patient/appointment/book",
                entity,
                String.class
        );

        assertTrue(
                response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                        response.getStatusCode() == HttpStatus.FORBIDDEN
        );
    }

    // =========================================================================
    // BƯỚC 4 – APPOINTMENT LIST API (GET /api/patient/appointment/list)
    // =========================================================================

    @Test
    void getAppointments_shouldReturn200_whenAuthenticated() {
        HttpHeaders headers = patientCookieHeader(validToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/patient/appointment/list",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAppointments_shouldReturn401_whenNoToken() {
        HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/patient/appointment/list",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertTrue(
                response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                        response.getStatusCode() == HttpStatus.FORBIDDEN
        );
    }

    // =========================================================================
    // BƯỚC 5 – APPOINTMENT STATUS API (Dùng PUT thay vì POST)
    // =========================================================================

    @Test
    void updateAppointmentStatus_shouldReturn200_whenValid() {
        Appointment appointment = new Appointment();
        appointment.setPatient(savedPatient);
        appointment.setDoctor(savedDoctor);
        appointment.setScheduleTime(LocalDateTime.now().plusDays(1));
        appointment.setStatus("PENDING");
        Appointment savedAppointment = appointmentRepository.save(appointment);

        String url = baseUrl() + "/api/patient/appointment/update/" + savedAppointment.getId() + "/CONFIRMED";

        HttpHeaders headers = jsonWithCookie("jwt-patient-token", validToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST, // Chốt sổ là dùng POST theo đúng yêu cầu task
                entity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}