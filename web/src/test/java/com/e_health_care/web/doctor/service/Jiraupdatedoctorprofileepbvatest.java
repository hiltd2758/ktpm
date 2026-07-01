package com.e_health_care.web.doctor.service;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
@Feature("Update Doctor Profile")
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
    @DisplayName("TC01 [V1,V2,V3,V4,V5,V6]: tất cả hợp lệ, không upload ảnh -> cập nhật thành công")
    @Story("Cập nhật hồ sơ thành công không kèm ảnh")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra trường hợp nominal: tất cả đầu vào hợp lệ, không upload ảnh đại diện. " +
            "Kỳ vọng hệ thống cập nhật thông tin bác sĩ vào DB mà không ném exception.")
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
    @DisplayName("TC02 [X1]: doctorId không tồn tại -> throw 'Không tìm thấy bác sĩ'")
    @Story("doctorId không tồn tại trong DB")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra khi doctorId không tìm thấy trong DB. " +
            "Kỳ vọng hệ thống ném RuntimeException với message chứa 'Không tìm thấy bác sĩ'.")
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
    @DisplayName("TC03 [X2]: firstName rỗng -> throw validation error")
    @Story("firstName bị rỗng")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra khi firstName là chuỗi rỗng. " +
            "Kỳ vọng hệ thống ném RuntimeException với thông báo lỗi validation tên bác sĩ.")
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
    @DisplayName("TC04 [X3]: lastName null -> throw validation error")
    @Story("lastName bị null")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra khi lastName là null. " +
            "Kỳ vọng hệ thống ném RuntimeException với thông báo lỗi validation họ bác sĩ.")
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
    @DisplayName("TC05 [X4,B1]: phone 9 ký tự -> throw validation error (biên dưới ngoài)")
    @Story("phone có 9 ký tự — biên dưới ngoài vùng hợp lệ")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra giá trị biên: phone có 9 ký tự (ngay dưới ranh giới hợp lệ 10 ký tự). " +
            "Kỳ vọng hệ thống ném RuntimeException vì phone không đủ độ dài tối thiểu.")
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
    @DisplayName("TC06 [V4,B2]: phone 10 ký tự -> hợp lệ (biên dưới trong)")
    @Story("phone có 10 ký tự — biên dưới trong vùng hợp lệ")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra giá trị biên: phone có 10 ký tự (ranh giới dưới hợp lệ). " +
            "Kỳ vọng hệ thống chấp nhận và cập nhật thành công.")
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
    @DisplayName("TC07 [V4,B3]: phone 11 ký tự -> hợp lệ (biên trên trong)")
    @Story("phone có 11 ký tự — biên trên trong vùng hợp lệ")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra giá trị biên: phone có 11 ký tự (ranh giới trên hợp lệ). " +
            "Kỳ vọng hệ thống chấp nhận và cập nhật thành công.")
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
    @DisplayName("TC08 [X4,B4]: phone 12 ký tự -> throw validation error (biên trên ngoài)")
    @Story("phone có 12 ký tự — biên trên ngoài vùng hợp lệ")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra giá trị biên: phone có 12 ký tự (ngay trên ranh giới hợp lệ 11 ký tự). " +
            "Kỳ vọng hệ thống ném RuntimeException vì phone vượt quá độ dài tối đa.")
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
    @DisplayName("TC09 [X5]: field rỗng -> throw validation error")
    @Story("field chuyên khoa bị rỗng")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra khi field chuyên khoa là chuỗi rỗng. " +
            "Kỳ vọng hệ thống ném RuntimeException với thông báo lỗi validation chuyên khoa.")
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
    @DisplayName("TC10 [V1,V7]: upload avatar hợp lệ (image/jpeg) -> cập nhật avatar thành công")
    @Story("Upload ảnh đại diện hợp lệ định dạng image/jpeg")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra luồng upload avatar: file ảnh hợp lệ định dạng image/jpeg được gửi kèm. " +
            "Kỳ vọng hệ thống lưu file và cập nhật trường avatar trong DB.")
    void tc10_validAvatarUpload_shouldSaveAvatar() {
        Doctor existing = new Doctor();
        existing.setId(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(doctorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "avatarFile", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

        DoctorDTO input = dto(1L, "Nguyen", "Van A", "0901234567", "Cardiology");
        input.setAvatarFile(file);

        assertDoesNotThrow(() -> service.updateDoctorProfile(input));
        verify(doctorRepository).save(argThat(d -> d.getAvatar() != null && !d.getAvatar().isEmpty()));
    }

    // -----------------------------------------------------------------------
    // TC11 — X6 — upload file không phải ảnh (text/plain)
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC11 [X6]: upload file không phải ảnh (text/plain) -> throw error")
    @Story("Upload file không phải định dạng ảnh")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra khi người dùng upload file text/plain thay vì file ảnh. " +
            "Kỳ vọng hệ thống ném RuntimeException từ chối file không hợp lệ.")
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
    @DisplayName("TC12 [X1]: nhiều điều kiện sai -> chỉ throw lỗi doctor not found (kiểm tra đầu tiên)")
    @Story("Nhiều đầu vào sai — kiểm tra thứ tự ưu tiên validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra thứ tự ưu tiên: khi cả doctorId không tồn tại lẫn các trường khác đều sai, " +
            "hệ thống phải ném lỗi doctorId trước tiên vì đây là điều kiện được kiểm tra đầu tiên trong logic.")
    void tc12_multipleInvalid_shouldThrowDoctorNotFoundFirst() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        DoctorDTO input = dto(999L, "", null, "abc", "");

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.updateDoctorProfile(input));
        assertTrue(ex.getMessage().contains("Không tìm thấy bác sĩ"),
                "Phải throw lỗi doctorId trước các lỗi validation khác");
    }
}