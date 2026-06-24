package com.e_health_care.web.doctor.service;

import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void updateDoctorProfile_DoctorIdExists_CallsRepositorySave() {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(1L);
        dto.setFirstName("Van");
        dto.setLastName("A");

        Doctor mockDoctor = new Doctor();
        mockDoctor.setId(1L);
        Mockito.lenient().when(doctorRepository.findById(Mockito.any())).thenReturn(Optional.of(mockDoctor));

        doctorService.updateDoctorProfile(dto);
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    @Test
    void updateDoctorProfile_DoctorIdDoesNotExist_ThrowsException() {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(99L);
        Mockito.lenient().when(doctorRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateDoctorProfile(dto);
        });

        assertEquals("Không tìm thấy bác sĩ với ID: 99", exception.getMessage());
    }

    @Test
    void getDoctorByEmail_EmailExists_ReturnsDoctorDTO() {
        String email = "test@doctor.com";
        Doctor mockDoctor = new Doctor();
        mockDoctor.setId(1L);
        mockDoctor.setEmail(email);

        Mockito.lenient().when(doctorRepository.findByEmail(email)).thenReturn(mockDoctor);

        DoctorDTO result = doctorService.getDoctorByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void getDoctorByEmail_EmailDoesNotExist_ReturnsNull() {
        String email = "notfound@doctor.com";
        Mockito.lenient().when(doctorRepository.findByEmail(email)).thenReturn(null);

        DoctorDTO result = doctorService.getDoctorByEmail(email);

        assertNull(result);
    }
}