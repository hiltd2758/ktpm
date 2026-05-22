package com.e_health_care.web.patient.model;

import com.e_health_care.web.doctor.model.Doctor;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultation")
@Data
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "schedule_time", nullable = false)
    private LocalDateTime scheduleTime;

    @Column(length = 50)
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED
}
