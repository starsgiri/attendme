package com.example.demo.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Direct mapping to the specific attendance session
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession attendanceSession;

    // Student specific dynamic data
    private String name;
    private String branch;
    
    @Column(name = "study_year")
    private String year;
    
    private String rollNumber;

    @Column(length = 255)
    private String deviceFingerprint;

    // Captured location at the time of scanning/attendance
    private Double capturedLatitude;
    
    private Double capturedLongitude;

    @Column(nullable = false)
    private boolean isWithinRange;

    private LocalDateTime markedAt;

    @PrePersist
    protected void onMarked() {
        this.markedAt = LocalDateTime.now();
    }
}
