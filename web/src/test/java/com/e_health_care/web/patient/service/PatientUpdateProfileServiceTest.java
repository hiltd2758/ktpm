package com.e_health_care.web.patient.service;
 
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
 
class PatientUpdateProfileServiceTest extends BaseServiceTest {
 
    @Mock
    private PatientRepository patientRepository;
 
    @Mock
    private PasswordEncoder passwordEncoder; // ← Mock để Mockito inject được
 
    @InjectMocks
    private PatientUpdateProfileService service;
 
    // =========================================================
    // TC01 [V1,V2,V3,V4,V5] – nominal, không đổi password
    // =========================================================
    @Test
    @DisplayName("TC01 [V1,V2,V3,V4,V5]: cập nhật hợp lệ không có password -> save() được gọi")
    void tc01_updatePatient_noPassword_shouldSave() {
        Patient p = new Patient();
        p.setId(1L);
        p.setPassword("$2a$10$oldHashedPassword");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));
 
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setPhone("0909123456");
 
        service.updatePatient(dto, 1L);
 
        verify(patientRepository, times(1)).save(any());
        // password null → encode không được gọi
        verify(passwordEncoder, never()).encode(any());
    }
 
    // =========================================================
    // TC02 [X1] – patientId không tồn tại
    // =========================================================
    @Test
    @DisplayName("TC02 [X1]: patientId không tồn tại -> throw 'Patient not found'")
    void tc02_updatePatient_patientNotFound_shouldThrow() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());
 
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.updatePatient(new PatientDTO(), 99L));
        assertEquals("Patient not found", ex.getMessage());
    }
 
    // =========================================================
    // TC03 [V5,B1] – password rỗng, không đổi
    // =========================================================
    @Test
    @DisplayName("TC03 [V5,B1]: password rỗng -> password giữ nguyên, encode không được gọi")
    void tc03_updatePatient_emptyPassword_shouldNotChangePassword() {
        Patient p = new Patient();
        p.setId(1L);
        p.setPassword("$2a$10$oldHashedPassword");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));
 
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setPassword(""); // B1: rỗng
 
        service.updatePatient(dto, 1L);
 
        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        assertEquals("$2a$10$oldHashedPassword", captor.getValue().getPassword(),
                "Password rỗng không được làm thay đổi password hiện tại!");
        verify(passwordEncoder, never()).encode(any());
    }
 
    // =========================================================
    // TC04 [X5,B3] – BUG EHC-56: password có giá trị phải gọi encode()
    // =========================================================
    @Test
    @DisplayName("TC04 [X5,B3] EHC-56 BUG: password 'newpassword123' phải gọi passwordEncoder.encode()")
    void tc04_EHC56_passwordShouldCallEncode() {
        Patient p = new Patient();
        p.setId(1L);
        p.setPassword("$2a$10$oldHashedPassword");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));
        // Stub: khi encode("newpassword123") → trả về chuỗi BCrypt giả
        when(passwordEncoder.encode("newpassword123")).thenReturn("$2a$10$fakeHashForTest");
 
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setPassword("newpassword123"); // B3: nominal
 
        service.updatePatient(dto, 1L);
 
        // Verify encode() được gọi đúng 1 lần với đúng password
        verify(passwordEncoder, times(1)).encode("newpassword123");
 
        // Verify password lưu vào DB là giá trị sau encode, không phải plain text
        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        String savedPassword = captor.getValue().getPassword();
 
        assertNotEquals("newpassword123", savedPassword,
                "BUG EHC-56: Password lưu plain text, không qua passwordEncoder.encode()!");
        assertEquals("$2a$10$fakeHashForTest", savedPassword,
                "Password phải là giá trị trả về từ passwordEncoder.encode()!");
    }
 
    // =========================================================
    // TC05 [V6,B2] – password 1 ký tự (min+), phải encode
    // =========================================================
    @Test
    @DisplayName("TC05 [V6,B2]: password 1 ký tự -> encode() được gọi")
    void tc05_updatePatient_password1Char_shouldCallEncode() {
        Patient p = new Patient();
        p.setId(1L);
        p.setPassword("$2a$10$oldHashedPassword");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));
        when(passwordEncoder.encode("a")).thenReturn("$2a$10$fakeHash1Char");
 
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setPassword("a"); // B2: 1 ký tự
 
        service.updatePatient(dto, 1L);
 
        verify(passwordEncoder, times(1)).encode("a");
        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        assertNotEquals("a", captor.getValue().getPassword(),
                "Password 1 ký tự không được lưu plain text!");
    }
 
    // =========================================================
    // TC06 [V6,B5] – password 50 ký tự (max), phải encode
    // =========================================================
    @Test
    @DisplayName("TC06 [V6,B5]: password 50 ký tự -> encode() được gọi")
    void tc06_updatePatient_password50Chars_shouldCallEncode() {
        Patient p = new Patient();
        p.setId(1L);
        p.setPassword("$2a$10$oldHashedPassword");
        String pass50 = "a".repeat(50);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));
        when(passwordEncoder.encode(pass50)).thenReturn("$2a$10$fakeHash50Chars");
 
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setPassword(pass50); // B5: 50 ký tự
 
        service.updatePatient(dto, 1L);
 
        verify(passwordEncoder, times(1)).encode(pass50);
        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        assertNotEquals(pass50, captor.getValue().getPassword(),
                "Password 50 ký tự không được lưu plain text!");
    }
 
    // =========================================================
    // TC07 [V2,B6] – firstName 1 ký tự (biên min)
    // =========================================================
    @Test
    @DisplayName("TC07 [V2,B6]: firstName 1 ký tự -> hợp lệ, save() được gọi")
    void tc07_firstName_minBoundary_shouldSave() {
        Patient p = new Patient();
        p.setId(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));
 
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("J"); // B6
        dto.setLastName("Doe");
 
        service.updatePatient(dto, 1L);
        verify(patientRepository, times(1)).save(any());
    }
 
    // =========================================================
    // TC08 [V2,B10] – firstName 50 ký tự (biên max)
    // =========================================================
    @Test
    @DisplayName("TC08 [V2,B10]: firstName 50 ký tự -> hợp lệ, save() được gọi")
    void tc08_firstName_maxBoundary_shouldSave() {
        Patient p = new Patient();
        p.setId(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));
 
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("a".repeat(50)); // B10
        dto.setLastName("Doe");
 
        service.updatePatient(dto, 1L);
        verify(patientRepository, times(1)).save(any());
    }
 
    // =========================================================
    // TC09 [V4,B11] – phone rỗng (biên min)
    // =========================================================
    @Test
    @DisplayName("TC09 [V4,B11]: phone rỗng -> hợp lệ, save() được gọi")
    void tc09_phone_emptyBoundary_shouldSave() {
        Patient p = new Patient();
        p.setId(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));
 
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setPhone(""); // B11
 
        service.updatePatient(dto, 1L);
        verify(patientRepository, times(1)).save(any());
    }
 
    // =========================================================
    // TC10 [V4,B15] – phone 10 ký tự (biên max)
    // =========================================================
    @Test
    @DisplayName("TC10 [V4,B15]: phone 10 ký tự -> hợp lệ, save() được gọi")
    void tc10_phone_maxBoundary_shouldSave() {
        Patient p = new Patient();
        p.setId(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));
 
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setPhone("0909123456"); // B15
 
        service.updatePatient(dto, 1L);
        verify(patientRepository, times(1)).save(any());
    }
 
    // =========================================================
    // getPatientById tests
    // =========================================================
    @Test
    void getPatientById_shouldReturnDTO_whenFound() {
        Patient p = new Patient();
        p.setId(1L);
        p.setEmail("patient@test.com");
        p.setFirstName("John");
        p.setLastName("Doe");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));
 
        PatientDTO result = service.getPatientById(1L);
 
        assertNotNull(result);
        assertEquals("patient@test.com", result.getEmail());
        assertEquals("John", result.getFirstName());
    }
 
    @Test
    void getPatientById_shouldThrow_whenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getPatientById(99L));
    }
}