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

/**
 * Unit Test cho AdminAuthenticationService
 *
 * Chiến lược: AdminAuthenticationService dùng DaoAuthenticationProvider nội bộ,
 * nên chúng ta mock AdminDetailsService (thay vì AdminRepository trực tiếp)
 * và AdminJwtService để kiểm soát hoàn toàn luồng login().
 */
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

    // -----------------------------------------------------------------------
    // Test Case 2: Sai mật khẩu → login() phải trả về null
    // -----------------------------------------------------------------------

    @Test
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

    // -----------------------------------------------------------------------
    // Test Case 3: Đúng email và mật khẩu → login() phải trả về JWT token
    // -----------------------------------------------------------------------

    @Test
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
