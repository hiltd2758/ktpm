package com.e_health_care.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Description;

@SpringBootTest
@ActiveProfiles("test")
@Epic("Application Bootstrap")
@Feature("Spring Context Loading")
class WebApplicationTests {
	@Test
	@Story("Khởi động Spring Application Context")
	@Severity(SeverityLevel.BLOCKER)
	@Description("Kiểm tra Spring Boot Application Context khởi động thành công, toàn bộ bean được cấu hình đúng và không có lỗi khi load context.")
	void contextLoads() {}
}