package com.e_health_care.web.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.admin.dto.AdminDTO;
import com.e_health_care.web.admin.model.Admin;
import com.e_health_care.web.admin.repository.AdminRepository;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Unit Test cho AdminAuthenticationService
 *
 * Chiến lược: AdminAuthenticationService dùng DaoAuthenticationProvider nội bộ,
 * nên chúng ta mock AdminDetailsService (thay vì AdminRepository trực tiếp)
 * và AdminJwtService để kiểm soát hoàn toàn luồng login().
 */
// Fix: @Epic/@Feature dùng để nhóm test theo nghiệp vụ ở tab "Behaviors"
@Epic("Admin Management")
@Feature("Admin Authentication")
class AdminAuthenticationServiceTest extends BaseServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminJwtService jwtService;

    // AdminDetailsService được tạo thủ công để inject adminRepository mock vào
    private AdminDetailsService adminDetailsService;

    // PasswordEncoder thật — dùng BCrypt để mã hoá password giống production
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // System Under Test
    @InjectMocks
    private AdminAuthenticationService adminAuthenticationService;

    private static final String VALID_EMAIL    = "admin@hospital.com";
    private static final String VALID_PASSWORD = "Secret@123";
    private static final String FAKE_JWT       = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake";

    @BeforeEach
    void setUp() {
        // Tạo AdminDetailsService với adminRepository đã được mock
        adminDetailsService = new AdminDetailsService(adminRepository);

        // Inject các dependency vào service (vì dùng @Autowired field injection)
        ReflectionTestUtils.setField(adminAuthenticationService, "adminDetailsService", adminDetailsService);
        ReflectionTestUtils.setField(adminAuthenticationService, "passwordEncoder",     passwordEncoder);
        ReflectionTestUtils.setField(adminAuthenticationService, "jwtService",          jwtService);
    }

    @Test
    @Story("Đăng nhập với email không tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống trả về null và không gọi hàm sinh JWT token khi admin đăng nhập bằng email chưa được đăng ký trong cơ sở dữ liệu.")
    void login_emailNotFound_returnsNull() {
        // Arrange
        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setEmail("notexist@hospital.com");
        adminDTO.setPassword("AnyPassword1");

        // Repository không tìm thấy email → trả về null
        when(adminRepository.findByEmail("notexist@hospital.com")).thenReturn(null);

        // Act
        String result = adminAuthenticationService.login(adminDTO);

        // Assert
        assertNull(result, "login() phải trả về null khi email không tồn tại");
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @Story("Đăng nhập với mật khẩu sai")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống trả về null và không gọi hàm sinh JWT token khi admin nhập đúng email nhưng sai mật khẩu, đảm bảo không cấp quyền truy cập trái phép.")
    void login_wrongPassword_returnsNull() {
        // Arrange
        Admin admin = new Admin();
        admin.setEmail(VALID_EMAIL);
        admin.setPassword(passwordEncoder.encode(VALID_PASSWORD));

        when(adminRepository.findByEmail(VALID_EMAIL)).thenReturn(admin);

        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setEmail(VALID_EMAIL);
        adminDTO.setPassword("WrongPassword999"); // password SAI

        // Act
        String result = adminAuthenticationService.login(adminDTO);

        // Assert
        assertNull(result, "login() phải trả về null khi mật khẩu sai");
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @Story("Đăng nhập thành công với email và mật khẩu hợp lệ")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra hệ thống trả về đúng JWT token khi admin đăng nhập với email và mật khẩu khớp với thông tin đã lưu trong cơ sở dữ liệu, đồng thời xác minh hàm sinh token được gọi đúng một lần.")
    void login_correctCredentials_returnsJwtToken() {
        // Arrange
        Admin admin = new Admin();
        admin.setEmail(VALID_EMAIL);
        admin.setPassword(passwordEncoder.encode(VALID_PASSWORD)); // password đúng đã hash

        when(adminRepository.findByEmail(VALID_EMAIL)).thenReturn(admin);
        when(jwtService.generateToken(VALID_EMAIL)).thenReturn(FAKE_JWT);

        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setEmail(VALID_EMAIL);
        adminDTO.setPassword(VALID_PASSWORD); // password ĐÚNG

        // Act
        String result = adminAuthenticationService.login(adminDTO);

        // Assert
        assertNotNull(result, "login() phải trả về JWT token khi thông tin đăng nhập đúng");
        assertEquals(FAKE_JWT, result, "Giá trị JWT token phải khớp với token do jwtService tạo ra");
        verify(jwtService, times(1)).generateToken(VALID_EMAIL);
    }
}