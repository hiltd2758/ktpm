package com.e_health_care.web.doctor.service;

import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    public DoctorDTO getDoctorByEmail(String email) {
        Doctor doctor = doctorRepository.findByEmail(email);
        if (doctor == null) {
            return null;
        }
        return mapToDTO(doctor);
    }

    @Transactional
    public void updateDoctorProfile(DoctorDTO doctorDTO) {
        Optional<Doctor> optionalDoctor = doctorRepository.findById(doctorDTO.getId());

        if (optionalDoctor.isPresent()) {
            Doctor doctor = optionalDoctor.get();
            // Cập nhật thông tin cơ bản
            doctor.setFirstName(doctorDTO.getFirstName());
            doctor.setLastName(doctorDTO.getLastName());
            doctor.setPhone(doctorDTO.getPhone());
            doctor.setAddress(doctorDTO.getAddress());
            doctor.setField(doctorDTO.getField());

            // --- XỬ LÝ UPLOAD ẢNH (ĐÃ SỬA LẠI ĐƯỜNG DẪN) ---
            MultipartFile file = doctorDTO.getAvatarFile();

            if (file != null && !file.isEmpty()) {
                try {
                    // 1. Tạo tên file duy nhất: UUID + Tên gốc
                    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

                    // 2. Đường dẫn lưu file (QUAN TRỌNG: Phải khớp với bên Patient và WebConfig)
                    // Sửa đường dẫn src/... thành đường dẫn đầy đủ từ thư mục gốc dự án
                    Path uploadPath = Paths.get("DoAnThucTeCNPM/web/src/main/resources/static/img/avatars/");

                    // Tạo thư mục nếu chưa có
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    // 3. Lưu file vào ổ cứng
                    try (InputStream inputStream = file.getInputStream()) {
                        Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                    }

                    // 4. Lưu tên file vào Database
                    doctor.setAvatar(fileName);

                    // In ra console để kiểm tra
                    System.out.println("Doctor Avatar saved to: " + uploadPath.resolve(fileName).toAbsolutePath());

                } catch (IOException e) {
                    throw new RuntimeException("Không thể lưu file ảnh: " + e.getMessage());
                }
            }

            // Logic cũ của bạn: nếu không up ảnh mới mà DTO có string avatar thì giữ nguyên (thường thì không cần thiết nếu logic trên đã set, nhưng giữ lại cũng không sao)
            // if (doctorDTO.getAvatar() != null && !doctorDTO.getAvatar().isEmpty()) { ... }

            doctorRepository.save(doctor);
        } else {
            throw new RuntimeException("Không tìm thấy bác sĩ với ID: " + doctorDTO.getId());
        }
    }

    private DoctorDTO mapToDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setEmail(doctor.getEmail());
        dto.setPhone(doctor.getPhone());
        dto.setAddress(doctor.getAddress());
        dto.setField(doctor.getField());
        dto.setAvatar(doctor.getAvatar()); // Map avatar
        return dto;
    }
}