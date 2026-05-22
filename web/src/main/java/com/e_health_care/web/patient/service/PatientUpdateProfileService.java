package com.e_health_care.web.patient.service;

import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class PatientUpdateProfileService {

    @Autowired
    private PatientRepository patientRepository;

    // Hàm lấy thông tin (giữ nguyên, nhưng nhớ map thêm avatar)
    public PatientDTO getPatientById(Long id) {
        // 1. Tìm bệnh nhân trong DB
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // 2. Tạo DTO rỗng
        PatientDTO dto = new PatientDTO();

        // 3. Chép dữ liệu từ Patient (Entity) sang PatientDTO
        dto.setId(patient.getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setEmail(patient.getEmail());           // <--- Thêm dòng này
        dto.setPhone(patient.getPhone());           // <--- Thêm dòng này
        dto.setAddress(patient.getAddress());       // <--- Thêm dòng này
        dto.setDateOfBirth(patient.getDateOfBirth()); // <--- Thêm dòng này
        dto.setMedicalHistory(patient.getMedicalHistory()); // <--- Thêm dòng này
        dto.setAvatar(patient.getAvatar());

        // Lưu ý: Không set password vào DTO để tránh lộ mật khẩu cũ ra giao diện
        // dto.setPassword(patient.getPassword()); // Không làm dòng này

        return dto;
    }

    // Hàm update (Sửa lại để xử lý ảnh)
    public void updatePatient(PatientDTO patientDTO, Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Cập nhật thông tin cơ bản
        patient.setFirstName(patientDTO.getFirstName());
        patient.setLastName(patientDTO.getLastName());
        patient.setPhone(patientDTO.getPhone());
        patient.setAddress(patientDTO.getAddress());
        patient.setMedicalHistory(patientDTO.getMedicalHistory());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());

        // Xử lý mật khẩu nếu có (giữ nguyên logic cũ của bạn)
        if (patientDTO.getPassword() != null && !patientDTO.getPassword().isEmpty()) {
            patient.setPassword(patientDTO.getPassword());
        }

        // --- XỬ LÝ UPLOAD ẢNH ---
        // --- XỬ LÝ UPLOAD ẢNH (SỬA LẠI ĐỂ HIỆN NGAY LẬP TỨC) ---
        MultipartFile file = patientDTO.getAvatarFile();
        if (file != null && !file.isEmpty()) {
            try {
                // 1. Tạo tên file duy nhất
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

                // 2. Định nghĩa đường dẫn
                // A. Đường dẫn gốc (Source Code) - Để lưu giữ lâu dài
                Path srcPath = Paths.get("DoAnThucTeCNPM/web/src/main/resources/static/img/avatars/");

                // B. Đường dẫn Runtime (Target) - Để hiện ảnh ngay lập tức trên web
                Path targetPath = Paths.get("DoAnThucTeCNPM/web/target/classes/static/img/avatars/");

                // Tạo thư mục nếu chưa có
                if (!Files.exists(srcPath)) Files.createDirectories(srcPath);
                if (!Files.exists(targetPath)) Files.createDirectories(targetPath);

                // 3. Lưu file vào SRC (Lưu chính)
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, srcPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                }

                // 4. Lưu file vào TARGET (Lưu phụ để hiển thị ngay)
                // Lưu ý: Phải mở luồng mới (getInputStream) vì luồng cũ đã đóng sau khi copy
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, targetPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                }

                // 5. Lưu tên file vào database
                patient.setAvatar(fileName);

                // Debug: In ra để kiểm tra
                System.out.println("Đã lưu ảnh vào SRC: " + srcPath.resolve(fileName).toAbsolutePath());
                System.out.println("Đã lưu ảnh vào TARGET: " + targetPath.resolve(fileName).toAbsolutePath());

            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage());
            }
        }

        patientRepository.save(patient);
    }
}
