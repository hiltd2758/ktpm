package com.e_health_care.web.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.e_health_care.web.admin.model.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>{
    Admin findByEmail(String email);
}
