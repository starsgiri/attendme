package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "attendance_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String sessionName;

    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;

    // Stores which fields the creator activated for this session
    // e.g., {"Name": true, "Branch": true, "Year": false}
    @ElementCollection
    @CollectionTable(name = "session_activated_fields", joinColumns = @JoinColumn(name = "session_id"))
    @MapKeyColumn(name = "field_name")
    @Column(name = "is_active")
    private Map<String, Boolean> activatedFields = new HashMap<>();
    
    // Geofencing coordinates
    private Double targetLatitude;
    
    private Double targetLongitude;
    
    // Allowed radius in meters
    private Double allowedRadius;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
