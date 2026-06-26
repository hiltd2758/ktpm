package com.e_health_care.web.doctor.service;

<<<<<<< HEAD
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JIRADoctorUpdateProfileEpBvaTest {
=======
import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * EHC-55
 * Equivalence Partitioning + Boundary Value Analysis
 * cho updateDoctorProfile() — DoctorService
 *
 * Tag mapping:
 *   V1=id hợp lệ (doctor tồn tại)        X1=id không tồn tại
 *   V2=address mới khác address cũ       X2=address mới = null/rỗng (biên)
 *   V3=firstName/lastName/phone/field hợp lệ
 *
 * BUG: dòng "doctor.setAddress(doctorDTO.getAddress());" bị comment trong
 * DoctorService.updateDoctorProfile() => address mới KHÔNG được lưu xuống DB,
 * dù response trả về 200 OK "Cập nhật hồ sơ thành công".
 */
class JIRADoctorUpdateProfileEpBvaTest extends BaseServiceTest {
>>>>>>> 1379a2cc685f061695475797125ac0f705332984

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

<<<<<<< HEAD
    // Dùng primitive long để khớp với Doctor.id
    private Doctor existingDoctor(long id, String oldAddress) {
        Doctor d = new Doctor();
        d.setId(id);           // primitive long — khớp với model
=======
    private Doctor existingDoctor(String oldAddress) {
        Doctor d = new Doctor();
        d.setId(1L);
>>>>>>> 1379a2cc685f061695475797125ac0f705332984
        d.setEmail("doctor1@ehc.com");
        d.setFirstName("Van");
        d.setLastName("Nguyen");
        d.setPhone("0900000000");
        d.setAddress(oldAddress);
        d.setField("Tim mach");
        return d;
    }

<<<<<<< HEAD
    private DoctorDTO request(Long id, String firstName, String lastName,
                              String phone, String field, String address) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(id);         // Long wrapper — khớp với DoctorDTO.id
=======
    private DoctorDTO request(long id, String firstName, String lastName,
                              String phone, String field, String address) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(id);
>>>>>>> 1379a2cc685f061695475797125ac0f705332984
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setPhone(phone);
        dto.setField(field);
        dto.setAddress(address);
        return dto;
    }

<<<<<<< HEAD
    // TC01
    @Test
    @DisplayName("TC01 [BUG][V1,V2]: doi address -> address moi PHAI duoc luu (EHC-55)")
    void tc01_changeAddress_shouldPersistNewAddress() {
        Doctor existing = existingDoctor(1L, "123 Le Loi, Q1, TP.HCM");

        // Mock theo đúng object Long(1) mà DTO sẽ truyền vào
        when(doctorRepository.findById(Long.valueOf(1L)))
                .thenReturn(Optional.of(existing));

        DoctorDTO input = request(Long.valueOf(1L), "Van", "Nguyen",
                "0900000000", "Tim mach", "456 Nguyen Trai, Q5, TP.HCM");
=======
    // TC01 — V1,V2 — nominal: đổi address sang giá trị khác
    @Test
    @DisplayName("TC01 [BUG][V1,V2]: đổi address -> address mới PHẢI được lưu, nhưng thực tế không")
    void tc01_changeAddress_shouldPersistNewAddress() {
        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(existingDoctor("123 Le Loi, Q1, TP.HCM")));

        DoctorDTO input = request(1L, "Van", "Nguyen", "0900000000",
                "Tim mach", "456 Nguyen Trai, Q5, TP.HCM");
>>>>>>> 1379a2cc685f061695475797125ac0f705332984

        doctorService.updateDoctorProfile(input);

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());

        assertEquals("456 Nguyen Trai, Q5, TP.HCM", captor.getValue().getAddress(),
<<<<<<< HEAD
                "EHC-55: address phai duoc cap nhat sang gia tri moi");
    }

    // TC02
    @Test
    @DisplayName("TC02 [V1,V3]: cac field khac duoc cap nhat dung, khong bi anh huong")
    void tc02_otherFields_shouldUpdateCorrectly() {
        Doctor existing = existingDoctor(1L, "789 Vo Van Tan");

        when(doctorRepository.findById(Long.valueOf(1L)))
                .thenReturn(Optional.of(existing));

        DoctorDTO input = request(Long.valueOf(1L), "Thanh", "Tran",
                "0911111111", "Da khoa", "789 Vo Van Tan");
=======
                "EHC-55: address khong duoc cap nhat vi dong setAddress bi comment trong DoctorService");
    }

    // TC02 — V1,V3 — các field khác vẫn được cập nhật đúng
    @Test
    @DisplayName("TC02 [V1,V3]: firstName/lastName/phone/field được cập nhật đúng")
    void tc02_otherFields_shouldUpdateCorrectly() {
        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(existingDoctor("789 Vo Van Tan")));

        DoctorDTO input = request(1L, "Thanh", "Tran", "0911111111", "Da khoa", "789 Vo Van Tan");
>>>>>>> 1379a2cc685f061695475797125ac0f705332984

        doctorService.updateDoctorProfile(input);

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        Doctor saved = captor.getValue();

        assertEquals("Thanh", saved.getFirstName());
        assertEquals("Tran", saved.getLastName());
        assertEquals("0911111111", saved.getPhone());
        assertEquals("Da khoa", saved.getField());
    }

<<<<<<< HEAD
    // TC03
    @Test
    @DisplayName("TC03 [X1]: id khong ton tai -> throw RuntimeException, khong goi save()")
    void tc03_doctorNotFound_shouldThrow() {
        when(doctorRepository.findById(Long.valueOf(999L)))
                .thenReturn(Optional.empty());

        DoctorDTO input = request(Long.valueOf(999L), "X", "Y",
                "0900000000", "Da khoa", "Dia chi bat ky");
=======
    // TC03 — X1 — id không tồn tại
    @Test
    @DisplayName("TC03 [X1]: id không tồn tại -> throw 'Không tìm thấy bác sĩ'")
    void tc03_doctorNotFound_shouldThrow() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        DoctorDTO input = request(999L, "X", "Y", "0900000000", "Da khoa", "Dia chi bat ky");
>>>>>>> 1379a2cc685f061695475797125ac0f705332984

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> doctorService.updateDoctorProfile(input));

<<<<<<< HEAD
        assertEquals(true, ex.getMessage().contains("999"),
                "Message phai chua ID 999, actual: " + ex.getMessage());

        verify(doctorRepository, never()).save(any());
    }

    // TC04
    @ParameterizedTest(name = "TC04.{index} address = \"{0}\"")
    @DisplayName("TC04 [X2]: address rong -> khong throw, save() van duoc goi")
    @CsvSource({ "''", "' '" })
    void tc04_blankAddress_edgeCase_shouldNotCrash(String newAddress) {
        Doctor existing = existingDoctor(1L, "123 Le Loi, Q1, TP.HCM");

        when(doctorRepository.findById(Long.valueOf(1L)))
                .thenReturn(Optional.of(existing));

        DoctorDTO input = request(Long.valueOf(1L), "Van", "Nguyen",
                "0900000000", "Tim mach", newAddress);

        assertDoesNotThrow(() -> doctorService.updateDoctorProfile(input));

        verify(doctorRepository).save(any(Doctor.class));
=======
        assertTrue(ex.getMessage().contains("Không tìm thấy bác sĩ"));
        verify(doctorRepository, never()).save(any());
    }

    // TC04 — X2 — address mới null/rỗng (biên) -> không crash nhưng vẫn lộ bug
    @ParameterizedTest(name = "TC04.{index} [X2] address mới = \"{0}\"")
    @DisplayName("TC04 [X2]: address null/rỗng -> không throw, address cũ vẫn giữ nguyên do bug")
    @CsvSource({
            "''",
            "' '"
    })
    void tc04_blankAddress_edgeCase_shouldNotCrash(String newAddress) {
        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(existingDoctor("123 Le Loi, Q1, TP.HCM")));

        DoctorDTO input = request(1L, "Van", "Nguyen", "0900000000", "Tim mach", newAddress);

        assertDoesNotThrow(() -> doctorService.updateDoctorProfile(input));

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());

        assertEquals("123 Le Loi, Q1, TP.HCM", captor.getValue().getAddress(),
                "Du input la gi, address cu khong doi vi dong setAddress bi comment");
>>>>>>> 1379a2cc685f061695475797125ac0f705332984
    }
}