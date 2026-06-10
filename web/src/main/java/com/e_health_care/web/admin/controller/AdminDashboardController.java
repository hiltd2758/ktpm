package com.e_health_care.web.admin.controller;

import com.e_health_care.web.admin.dto.*;
import com.e_health_care.web.admin.service.AdminManagementService;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST API Controller for Patient Management
 * 
 * Provides JSON API endpoints for CRUD operations on patients.
 * This is a new API layer separate from the HTML view-based AdminDashboardController.
 * 
 * Base URL: /admin/api
 * 
 * Endpoints:
 * - GET    /patients              - Get all patients
 * - GET    /patients/{id}         - Get patient by ID
 * - POST   /patients              - Create new patient
 * - PUT    /patients/{id}         - Update patient info
 * - PUT    /patients/{id}/password - Update patient password
 * - DELETE /patients/{id}         - Delete patient
 */
@RestController
@RequestMapping("/admin/api")
public class AdminDashboardController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AdminManagementService adminManagementService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Helper method to safely create response maps with null value support
     * Uses HashMap instead of Map.of() to allow null values
     */
    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    /**
     * GET /admin/api/patients
     * 
     * Retrieve all patients for dashboard display
     * 
     * @return ResponseEntity with status 200 and list of patients
     */
    @GetMapping("/patients")
    public ResponseEntity<?> getAllPatients() {
        try {
            List<Patient> patients = patientRepository.findAll();
            
            // Use HashMap to safely handle potential null values
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Patients retrieved successfully");
            response.put("data", patients);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(
                false,
                "Error retrieving patients: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()),
                null
            ));
        }
    }

    /**
     * GET /admin/api/patients/{id}
     * 
     * Retrieve a specific patient by ID
     * 
     * @param id Patient ID
     * @return ResponseEntity with status 200 if found, 404 if not found
     */
    @GetMapping("/patients/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable long id) {
        try {
            var optionalPatient = adminManagementService.getPatientById(id);
            
            if (optionalPatient.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Patient retrieved successfully");
                response.put("data", optionalPatient.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(createResponse(
                    false,
                    "Patient not found with ID: " + id,
                    null
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(
                false,
                "Error retrieving patient: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()),
                null
            ));
        }
    }

    /**
     * POST /admin/api/patients
     * 
     * Create a new patient
     * 
     * @param dto PatientCreateDTO containing patient data
     * @return ResponseEntity with status 201 if created, 400 if validation fails
     */
    @PostMapping("/patients")
    public ResponseEntity<?> createPatient(@RequestBody PatientCreateDTO dto) {
        try {
            // Validate DTO not null
            if (dto == null) {
                return ResponseEntity.badRequest().body(createResponse(
                    false,
                    "Request body cannot be empty",
                    null
                ));
            }

            // Validation: Check required fields
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createResponse(
                    false,
                    "Email is required",
                    null
                ));
            }

            if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createResponse(
                    false,
                    "Password is required",
                    null
                ));
            }

            // Check if email already exists
            if (patientRepository.findByEmail(dto.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(createResponse(
                    false,
                    "Email already exists: " + dto.getEmail(),
                    null
                ));
            }

            // Create new patient
            Patient patient = new Patient();
            patient.setEmail(dto.getEmail());
            patient.setFirstName(dto.getFirstName() != null ? dto.getFirstName() : "");
            patient.setLastName(dto.getLastName() != null ? dto.getLastName() : "");
            patient.setAddress(dto.getAddress() != null ? dto.getAddress() : "");
            patient.setPhone(dto.getPhone() != null ? dto.getPhone() : "");
            patient.setDateOfBirth(dto.getDateOfBirth());
            patient.setMedicalHistory(dto.getMedicalHistory() != null ? dto.getMedicalHistory() : "");
            patient.setAvatar(dto.getAvatar() != null ? dto.getAvatar() : "");
            patient.setPassword(passwordEncoder.encode(dto.getPassword()));

            Patient savedPatient = patientRepository.save(patient);

            // Use HashMap to safely handle potential null values
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Patient created successfully");
            response.put("data", savedPatient);

            return ResponseEntity.status(201).body(response);
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body(createResponse(
                false,
                "Invalid request format: " + (e.getMessage() != null ? e.getMessage() : "Null pointer"),
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(
                false,
                "Error creating patient: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()),
                null
            ));
        }
    }

    /**
     * PUT /admin/api/patients/{id}
     * 
     * Update patient information (excluding password)
     * 
     * @param id Patient ID
     * @param dto PatientUpdateDTO containing updated data
     * @return ResponseEntity with status 200 if updated, 404 if not found
     */
    @PutMapping("/patients/{id}")
    public ResponseEntity<?> updatePatient(
            @PathVariable long id,
            @RequestBody PatientUpdateDTO dto) {
        try {
            // Validate DTO not null
            if (dto == null) {
                return ResponseEntity.badRequest().body(createResponse(
                    false,
                    "Request body cannot be empty",
                    null
                ));
            }

            var optionalPatient = adminManagementService.getPatientById(id);
            if (optionalPatient.isEmpty()) {
                return ResponseEntity.status(404).body(createResponse(
                    false,
                    "Patient not found with ID: " + id,
                    null
                ));
            }

            Patient patient = optionalPatient.get();
            
            // Update fields
            if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
                // Check if email is already used by another patient
                var existingEmail = patientRepository.findByEmail(dto.getEmail());
                if (existingEmail.isPresent() && existingEmail.get().getId() != id) {
                    return ResponseEntity.badRequest().body(createResponse(
                        false,
                        "Email already used by another patient: " + dto.getEmail(),
                        null
                    ));
                }
                patient.setEmail(dto.getEmail());
            }
            if (dto.getFirstName() != null) patient.setFirstName(dto.getFirstName());
            if (dto.getLastName() != null) patient.setLastName(dto.getLastName());
            if (dto.getAddress() != null) patient.setAddress(dto.getAddress());
            if (dto.getPhone() != null) patient.setPhone(dto.getPhone());
            if (dto.getDateOfBirth() != null) patient.setDateOfBirth(dto.getDateOfBirth());
            if (dto.getMedicalHistory() != null) patient.setMedicalHistory(dto.getMedicalHistory());
            if (dto.getAvatar() != null) patient.setAvatar(dto.getAvatar());

            Patient updatedPatient = adminManagementService.updatePatientInformation(patient);

            // Use HashMap to safely handle potential null values
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Patient updated successfully");
            response.put("data", updatedPatient);

            return ResponseEntity.ok(response);
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body(createResponse(
                false,
                "Invalid request format: " + (e.getMessage() != null ? e.getMessage() : "Null pointer"),
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(
                false,
                "Error updating patient: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()),
                null
            ));
        }
    }

    /**
     * PUT /admin/api/patients/{id}/password
     * 
     * Update patient password
     * 
     * @param id Patient ID
     * @param dto PasswordUpdateDTO containing new password
     * @return ResponseEntity with status 200 if updated, 404 if not found
     */
    @PutMapping("/patients/{id}/password")
    public ResponseEntity<?> updatePatientPassword(
            @PathVariable long id,
            @RequestBody PasswordUpdateDTO dto) {
        try {
            if (dto.getNewPassword() == null || dto.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createResponse(
                    false,
                    "New password is required",
                    null
                ));
            }

            boolean updated = adminManagementService.updatePatientPassword(id, dto.getNewPassword());
            if (!updated) {
                return ResponseEntity.status(404).body(createResponse(
                    false,
                    "Patient not found with ID: " + id,
                    null
                ));
            }

            return ResponseEntity.ok(createResponse(
                true,
                "Patient password updated successfully",
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createResponse(
                false,
                "Error updating password: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()),
                null
            ));
        }
    }

    /**
     * DELETE /admin/api/patients/{id}
     * 
     * Delete a patient
     * 
     * @param id Patient ID
     * @return ResponseEntity with status 200 if deleted, 404 if not found
     */
    @DeleteMapping("/patients/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable long id) {
        try {
            var patient = adminManagementService.getPatientById(id);
            if (patient.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Patient not found with ID: " + id);
                response.put("data", null);
                return ResponseEntity.status(404).body(response);
            }

            adminManagementService.deletePatient(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Patient deleted successfully");
            response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error deleting patient: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}