package com.e_health_care.web.doctor.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void updateDoctorProfile_DoctorIdExists_CallsRepositorySave() {
        // Kịch bản: ID hợp lệ -> Phải gọi hàm save()
        DoctorDTO dto = new DoctorDTO();
        dto.setId(1L);
        dto.setFirstName("Kha");
        dto.setLastName("Nhu");

        Doctor mockDoctor = new Doctor();
        mockDoctor.setId(1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(mockDoctor));

        doctorService.updateDoctorProfile(dto);

        // Kiểm tra xem database có được lệnh lưu (save) hay không
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    @Test
    void updateDoctorProfile_DoctorIdDoesNotExist_ThrowsException() {
        // Kịch bản: Truyền ID ảo (99L) -> Phải ném ra lỗi RuntimeException
        DoctorDTO dto = new DoctorDTO();
        dto.setId(99L);

        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateDoctorProfile(dto);
        });

        // Kiểm tra luôn câu báo lỗi có khớp với code của Dev không
        assertEquals("Không tìm thấy bác sĩ với ID: 99", exception.getMessage());
    }

    @Test
    void getDoctorByEmail_EmailExists_ReturnsDoctorDTO() {
        // Kịch bản: Tìm bằng Email có thật -> Trả về DTO
        String email = "test@doctor.com";
        Doctor mockDoctor = new Doctor();
        mockDoctor.setId(1L);
        mockDoctor.setEmail(email);

        when(doctorRepository.findByEmail(email)).thenReturn(mockDoctor);

        DoctorDTO result = doctorService.getDoctorByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void getDoctorByEmail_EmailDoesNotExist_ReturnsNull() {
        // Kịch bản: Tìm Email không có thật -> Trả về null
        String email = "notfound@doctor.com";
        when(doctorRepository.findByEmail(email)).thenReturn(null);

        DoctorDTO result = doctorService.getDoctorByEmail(email);

        assertNull(result);
    }

    @Test
void updateDoctorProfile_AddressIsUpdated_SavesNewAddressToDatabase() {
    // Kịch bản: Doctor đổi address -> DB phải lưu địa chỉ mới (EHC-55)
    String oldAddress = "123 Đường Cũ, Quận 1";
    String newAddress = "456 Đường Mới, Quận 3";

    Doctor existingDoctor = new Doctor();
    existingDoctor.setId(1L);
    existingDoctor.setFirstName("Nguyen");
    existingDoctor.setLastName("Van A");
    existingDoctor.setPhone("0901234567");
    existingDoctor.setAddress(oldAddress);
    existingDoctor.setField("Cardiology");

    DoctorDTO updateRequest = new DoctorDTO();
    updateRequest.setId(1L);
    updateRequest.setFirstName("Nguyen");
    updateRequest.setLastName("Van A");
    updateRequest.setPhone("0901234567");
    updateRequest.setAddress(newAddress); // <-- đổi sang địa chỉ mới
    updateRequest.setField("Cardiology");

    when(doctorRepository.findById(1L)).thenReturn(Optional.of(existingDoctor));

    // Capture đối tượng Doctor được truyền vào save()
    when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

    doctorService.updateDoctorProfile(updateRequest);

    // Verify save() được gọi với address mới
    verify(doctorRepository).save(argThat(savedDoctor ->
        newAddress.equals(savedDoctor.getAddress())
    ));
}

@Test
void updateDoctorProfile_AddressChange_DoesNotAffectOtherFields() {
    // Kịch bản: Chỉ đổi address -> các field khác không bị mất (không regression)
    Doctor existingDoctor = new Doctor();
    existingDoctor.setId(2L);
    existingDoctor.setFirstName("Tran");
    existingDoctor.setLastName("Thi B");
    existingDoctor.setPhone("0987654321");
    existingDoctor.setAddress("Old Address");
    existingDoctor.setField("Neurology");

    DoctorDTO updateRequest = new DoctorDTO();
    updateRequest.setId(2L);
    updateRequest.setFirstName("Tran");
    updateRequest.setLastName("Thi B");
    updateRequest.setPhone("0987654321");
    updateRequest.setAddress("New Address");
    updateRequest.setField("Neurology");

    when(doctorRepository.findById(2L)).thenReturn(Optional.of(existingDoctor));
    when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

    doctorService.updateDoctorProfile(updateRequest);

    verify(doctorRepository).save(argThat(savedDoctor ->
        "New Address".equals(savedDoctor.getAddress())     // address mới được lưu
        && "Tran".equals(savedDoctor.getFirstName())       // firstName không đổi
        && "Neurology".equals(savedDoctor.getField())      // field không đổi
        && "0987654321".equals(savedDoctor.getPhone())     // phone không đổi
    ));
}
}