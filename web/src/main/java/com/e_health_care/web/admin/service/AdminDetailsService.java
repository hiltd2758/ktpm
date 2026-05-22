package com.e_health_care.web.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.e_health_care.web.admin.model.Admin;
import com.e_health_care.web.admin.model.AdminPrinciple;
import com.e_health_care.web.admin.repository.AdminRepository;

@Service
public class AdminDetailsService implements UserDetailsService {
    @Autowired
    private AdminRepository adminRepository;

    public AdminDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByEmail(username);
        if(admin == null) {
            throw new UsernameNotFoundException("The email is not found, please try again.");
        }
        return new AdminPrinciple(admin);
    }
}
