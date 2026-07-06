package com.e_health_care.web.doctor.service;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

/**
 * Equivalence Partitioning + Boundary Value Analysis
 * cho updateDoctorProfile() — DoctorService
 *
 * Tag mapping:
 *   V1 = doctorId tồn tại trong DB          X1 = doctorId không tồn tại
 *   V2 = firstName hợp lệ (không rỗng)      X2 = firstName rỗng / null
 *   V3 = lastName hợp lệ (không rỗng)       X3 = lastName rỗng / null
 *   V4 = phone hợp lệ (10 số)               X4 = phone không hợp lệ (sai định dạng)
 *   V5 = field hợp lệ (không rỗng)          X5 = field rỗng / null
 *   V6 = avatarFile = null (không upload)
 *   V7 = avatarFile hợp lệ (image/jpeg)     X6 = avatarFile không hợp lệ (không phải ảnh)
 *
 *   BVA cho phone (độ dài chuỗi số):
 *   B1 = phone 9 ký tự  (biên dưới ngoài – không hợp lệ)
 *   B2 = phone 10 ký tự (biên dưới trong – hợp lệ)
 *   B3 = phone 11 ký tự (biên trên trong – hợp lệ)
 *   B4 = phone 12 ký tự (biên trên ngoài – không hợp lệ)
 */
@Epic("Doctor Management")
@Feature("Update Doctor Profile (EP/BVA)")
class JIRAUpdateDoctorProfileEpBvaTest extends BaseServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService service;

    /** Helper tạo DoctorDTO */
    private DoctorDTO dto(Long id, String firstName, String lastName,
                          String phone, String field) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(id);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setPhone(phone);
        dto.setField(field);
        dto.setAvatarFile(null); // mặc định không upload ảnh
        return dto;
    }

    // -----------------------------------------------------------------------
    // TC01 — V1,V2,V3,V4,V5,V6 — nominal hoàn toàn hợp lệ, không upload ảnh
    // -----------------------------------------------------------------------
    @Test
    @Story("Nominal - toàn bộ dữ liệu hợp lệ")
    @DisplayName("TC01 [V1,V2,V3,V4,V5,V6]: tất cả hợp lệ, không upload ảnh -> cập nhật thành công")
    void tc01_allValid_noAvatar_shouldUpdateSuccessfully() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(doctorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DoctorDTO input = dto(1L, "Nguyen", "Van A", "0901234567", "Cardiology");

        assertDoesNotThrow(() -> service.updateDoctorProfile(input));
        verify(doctorRepository).save(argThat(d ->
                "Nguyen".equals(d.getFirstName()) &&
                        "Van A".equals(d.getLastName()) &&
                        "0901234567".equals(d.getPhone()) &&
                        "Cardiology".equals(d.getField())
        ));
    }

    // -----------------------------------------------------------------------
    // TC02 — X1 — doctorId không tồn tại
    // -----------------------------------------------------------------------
    @Test
    @Story("doctorId không tồn tại")
    @DisplayName("TC02 [X1]: doctorId không tồn tại -> throw 'Không tìm thấy bác sĩ'")
    void tc02_doctorNotFound_shouldThrow() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        DoctorDTO input = dto(999L, "Nguyen", "Van A", "0901234567", "Cardiology");

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.updateDoctorProfile(input));
        assertTrue(ex.getMessage().contains("Không tìm thấy bác sĩ"),
                "Message phải chứa 'Không tìm thấy bác sĩ', actual: " + ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // TC03 — X2 — firstName rỗng
    // -----------------------------------------------------------------------
    @Test
    @Story("firstName rỗng")
    @DisplayName("TC03 [X2]: firstName rỗng -> throw validation error")
    void tc03_emptyFirstName_shouldThrow() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));

        DoctorDTO input = dto(1L, "", "Van A", "0901234567", "Cardiology");

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.updateDoctorProfile(input));
        assertNotNull(ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // TC04 — X3 — lastName null
    // -----------------------------------------------------------------------
    @Test
    @Story("lastName null")
    @DisplayName("TC04 [X3]: lastName null -> throw validation error")
    void tc04_nullLastName_shouldThrow() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));

        DoctorDTO input = dto(1L, "Nguyen", null, "0901234567", "Cardiology");

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.updateDoctorProfile(input));
        assertNotNull(ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // TC05 — X4,B1 — phone 9 ký tự (biên dưới ngoài – không hợp lệ)
    // -----------------------------------------------------------------------
    @Test
    @Story("Phone boundary validation")
    @DisplayName("TC05 [X4,B1]: phone 9 ký tự -> throw validation error (biên dưới ngoài)")
    void tc05_phone9Chars_shouldThrow() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));

        DoctorDTO input = dto(1L, "Nguyen", "Van A", "090123456", "Cardiology"); // 9 ký tự

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.updateDoctorProfile(input));
        assertNotNull(ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // TC06 — V4,B2 — phone 10 ký tự (biên dưới trong – hợp lệ)
    // -----------------------------------------------------------------------
    @Test
    @Story("Phone boundary validation")
    @DisplayName("TC06 [V4,B2]: phone 10 ký tự -> hợp lệ (biên dưới trong)")
    void tc06_phone10Chars_shouldSucceed() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(doctorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DoctorDTO input = dto(1L, "Nguyen", "Van A", "0901234567", "Cardiology"); // 10 ký tự

        assertDoesNotThrow(() -> service.updateDoctorProfile(input));
        verify(doctorRepository).save(argThat(d -> "0901234567".equals(d.getPhone())));
    }

    // -----------------------------------------------------------------------
    // TC07 — V4,B3 — phone 11 ký tự (biên trên trong – hợp lệ)
    // -----------------------------------------------------------------------
    @Test
    @Story("Phone boundary validation")
    @DisplayName("TC07 [V4,B3]: phone 11 ký tự -> hợp lệ (biên trên trong)")
    void tc07_phone11Chars_shouldSucceed() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(doctorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DoctorDTO input = dto(1L, "Nguyen", "Van A", "09012345678", "Cardiology"); // 11 ký tự

        assertDoesNotThrow(() -> service.updateDoctorProfile(input));
        verify(doctorRepository).save(argThat(d -> "09012345678".equals(d.getPhone())));
    }

    // -----------------------------------------------------------------------
    // TC08 — X4,B4 — phone 12 ký tự (biên trên ngoài – không hợp lệ)
    // -----------------------------------------------------------------------
    @Test
    @Story("Phone boundary validation")
    @DisplayName("TC08 [X4,B4]: phone 12 ký tự -> throw validation error (biên trên ngoài)")
    void tc08_phone12Chars_shouldThrow() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));

        DoctorDTO input = dto(1L, "Nguyen", "Van A", "090123456789", "Cardiology"); // 12 ký tự

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.updateDoctorProfile(input));
        assertNotNull(ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // TC09 — X5 — field rỗng
    // -----------------------------------------------------------------------
    @Test
    @Story("field rỗng")
    @DisplayName("TC09 [X5]: field rỗng -> throw validation error")
    void tc09_emptyField_shouldThrow() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));

        DoctorDTO input = dto(1L, "Nguyen", "Van A", "0901234567", "");

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.updateDoctorProfile(input));
        assertNotNull(ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // TC10 — V1,V7 — upload avatar hợp lệ (image/jpeg)
    // -----------------------------------------------------------------------
    @Test
    @Story("Upload avatar")
    @DisplayName("TC10 [V1,V7]: upload avatar hợp lệ (image/jpeg) -> cập nhật avatar thành công")
    void tc10_validAvatarUpload_shouldSaveAvatar() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(doctorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "avatarFile", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

        DoctorDTO input = dto(1L, "Nguyen", "Van A", "0901234567", "Cardiology");
        input.setAvatarFile(file);

        // Không throw IOException → lưu thành công
        assertDoesNotThrow(() -> service.updateDoctorProfile(input));
        verify(doctorRepository).save(argThat(d -> d.getAvatar() != null && !d.getAvatar().isEmpty()));
    }

    // -----------------------------------------------------------------------
    // TC11 — X6 — upload file không phải ảnh (text/plain)
    // -----------------------------------------------------------------------
    @Test
    @Story("Upload avatar")
    @DisplayName("TC11 [X6]: upload file không phải ảnh (text/plain) -> throw error")
    void tc11_invalidFileType_shouldThrow() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));

        MockMultipartFile file = new MockMultipartFile(
                "avatarFile", "document.txt", "text/plain", "some text".getBytes());

        DoctorDTO input = dto(1L, "Nguyen", "Van A", "0901234567", "Cardiology");
        input.setAvatarFile(file);

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.updateDoctorProfile(input));
        assertNotNull(ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // TC12 — X1 (ưu tiên) — nhiều điều kiện sai: doctorId không tồn tại + firstName rỗng
    // -----------------------------------------------------------------------
    @Test
    @Story("Nhiều điều kiện sai cùng lúc")
    @DisplayName("TC12 [X1]: nhiều điều kiện sai -> chỉ throw lỗi doctor not found (kiểm tra đầu tiên)")
    void tc12_multipleInvalid_shouldThrowDoctorNotFoundFirst() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        DoctorDTO input = dto(999L, "", null, "abc", "");

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.updateDoctorProfile(input));
        assertTrue(ex.getMessage().contains("Không tìm thấy bác sĩ"),
                "Phải throw lỗi doctorId trước các lỗi validation khác");
    }
}