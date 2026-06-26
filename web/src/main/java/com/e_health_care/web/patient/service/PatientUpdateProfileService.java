package com.e_health_care.web.patient.service;
 
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
 
import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
 
@Service
public class PatientUpdateProfileService {
 
    @Autowired
    private PatientRepository patientRepository;
 
    @Autowired
    private PasswordEncoder passwordEncoder; // ← FIX EHC-56: thêm inject
 
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
 
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setEmail(patient.getEmail());
        dto.setPhone(patient.getPhone());
        dto.setAddress(patient.getAddress());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setMedicalHistory(patient.getMedicalHistory());
        dto.setAvatar(patient.getAvatar());
        // Không set password vào DTO để tránh lộ mật khẩu
        return dto;
    }
 
    public void updatePatient(PatientDTO patientDTO, Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
 
        patient.setFirstName(patientDTO.getFirstName());
        patient.setLastName(patientDTO.getLastName());
        patient.setPhone(patientDTO.getPhone());
        patient.setAddress(patientDTO.getAddress());
        patient.setMedicalHistory(patientDTO.getMedicalHistory());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
 
        // ✅ FIX EHC-56: encode password trước khi lưu
        if (patientDTO.getPassword() != null && !patientDTO.getPassword().isEmpty()) {
            patient.setPassword(passwordEncoder.encode(patientDTO.getPassword()));
        }
 
        MultipartFile file = patientDTO.getAvatarFile();
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
 
                Path srcPath = Paths.get("DoAnThucTeCNPM/web/src/main/resources/static/img/avatars/");
                Path targetPath = Paths.get("DoAnThucTeCNPM/web/target/classes/static/img/avatars/");
 
                if (!Files.exists(srcPath)) Files.createDirectories(srcPath);
                if (!Files.exists(targetPath)) Files.createDirectories(targetPath);
 
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, srcPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                }
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, targetPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                }
 
                patient.setAvatar(fileName);
 
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage());
            }
        }
 
        patientRepository.save(patient);
    }
}
 
