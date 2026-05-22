package com.e_health_care.web.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.e_health_care.web.admin.dto.AdminDTO;
import com.e_health_care.web.admin.service.AdminAuthenticationService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin")
public class AdminAuthenticationController {
    @Autowired
    private AdminAuthenticationService authService;

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("admin", new AdminDTO());
        return "admin/admin-login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute AdminDTO adminDTO, HttpServletResponse response) {
        String token = authService.login(adminDTO);

        if(token != null) {
            // FIX 1: Use the correct cookie name
            Cookie cookie = new Cookie("jwt-admin-token", token);   
            
            cookie.setHttpOnly(true);
            
            // FIX 2: Set to FALSE for development on HTTP
            cookie.setSecure(false); 
            
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/admin/login?error";
        }
    }
}