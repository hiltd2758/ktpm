package com.e_health_care.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Description;

@Epic("Shared Utilities")
@Feature("Password Hashing")
public class HashTest {
    @Test
    @Story("Sinh chuỗi mật khẩu đã mã hóa")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra BCryptPasswordEncoder sinh ra được chuỗi hash từ mật khẩu gốc mà không ném lỗi ngoại lệ.")
    void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("Hash: " + encoder.encode("password123"));
    }
}