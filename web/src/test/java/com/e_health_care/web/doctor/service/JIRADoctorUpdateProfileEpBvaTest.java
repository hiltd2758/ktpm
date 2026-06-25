package com.e_health_care.web.doctor.service;

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

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor existingDoctor(String oldAddress) {
        Doctor d = new Doctor();
        d.setId(1L);
        d.setEmail("doctor1@ehc.com");
        d.setFirstName("Van");
        d.setLastName("Nguyen");
        d.setPhone("0900000000");
        d.setAddress(oldAddress);
        d.setField("Tim mach");
        return d;
    }

    private DoctorDTO request(long id, String firstName, String lastName,
                              String phone, String field, String address) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(id);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setPhone(phone);
        dto.setField(field);
        dto.setAddress(address);
        return dto;
    }

    // TC01 — V1,V2 — nominal: đổi address sang giá trị khác
    @Test
    @DisplayName("TC01 [BUG][V1,V2]: đổi address -> address mới PHẢI được lưu, nhưng thực tế không")
    void tc01_changeAddress_shouldPersistNewAddress() {
        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(existingDoctor("123 Le Loi, Q1, TP.HCM")));

        DoctorDTO input = request(1L, "Van", "Nguyen", "0900000000",
                "Tim mach", "456 Nguyen Trai, Q5, TP.HCM");

        doctorService.updateDoctorProfile(input);

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());

        assertEquals("456 Nguyen Trai, Q5, TP.HCM", captor.getValue().getAddress(),
                "EHC-55: address khong duoc cap nhat vi dong setAddress bi comment trong DoctorService");
    }

    // TC02 — V1,V3 — các field khác vẫn được cập nhật đúng
    @Test
    @DisplayName("TC02 [V1,V3]: firstName/lastName/phone/field được cập nhật đúng")
    void tc02_otherFields_shouldUpdateCorrectly() {
        when(doctorRepository.findById(1L))
                .thenReturn(Optional.of(existingDoctor("789 Vo Van Tan")));

        DoctorDTO input = request(1L, "Thanh", "Tran", "0911111111", "Da khoa", "789 Vo Van Tan");

        doctorService.updateDoctorProfile(input);

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        Doctor saved = captor.getValue();

        assertEquals("Thanh", saved.getFirstName());
        assertEquals("Tran", saved.getLastName());
        assertEquals("0911111111", saved.getPhone());
        assertEquals("Da khoa", saved.getField());
    }

    // TC03 — X1 — id không tồn tại
    @Test
    @DisplayName("TC03 [X1]: id không tồn tại -> throw 'Không tìm thấy bác sĩ'")
    void tc03_doctorNotFound_shouldThrow() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        DoctorDTO input = request(999L, "X", "Y", "0900000000", "Da khoa", "Dia chi bat ky");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> doctorService.updateDoctorProfile(input));

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
    }
}