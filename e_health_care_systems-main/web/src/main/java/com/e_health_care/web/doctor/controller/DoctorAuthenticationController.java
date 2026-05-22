package com.e_health_care.web.doctor.controller;

import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.service.DoctorAuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doctor")
public class DoctorAuthenticationController {

    @Autowired
    private DoctorAuthenticationService authService;

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        // Trang login thường dùng layout riêng (không có header chứa avatar bác sĩ)
        // Nên không lo về biến global "doctor"
        model.addAttribute("doctor", new DoctorDTO());
        return "doctor/doctor-login";
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute DoctorDTO doctorDTO, HttpServletResponse response) {
        String token = authService.verify(doctorDTO);

        if (token != null) {
            // Tạo cookie chứa JWT
            Cookie cookie = new Cookie("jwt-doctor-token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // Nên bật nếu chạy HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // Ví dụ: tồn tại 7 ngày
            response.addCookie(cookie);
            return "redirect:/doctor/dashboard";
        } else {
            return "redirect:/doctor/login?error";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt-doctor-token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Xóa ngay lập tức
        response.addCookie(cookie);
        return "redirect:/doctor/login?logout";
    }
    @GetMapping("/index")
    public String showIndex() {
        return "doctor/doctor-index";
    }
}