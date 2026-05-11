package com.example.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Attendee;

@Repository
public interface AttendeeRepository extends JpaRepository<Attendee, Long> {
    List<Attendee> findByAttendanceSessionId(UUID sessionId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendee a WHERE a.attendanceSession.id = :sessionId AND a.deviceFingerprint = :deviceFingerprint")
    boolean existsBySessionAndDeviceFingerprint(@Param("sessionId") UUID sessionId, @Param("deviceFingerprint") String deviceFingerprint);
    
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendee a WHERE a.attendanceSession.id = :sessionId AND LOWER(TRIM(a.rollNumber)) = LOWER(TRIM(:rollNumber))")
    boolean existsBySessionAndRollNumberNormalized(@Param("sessionId") UUID sessionId, @Param("rollNumber") String rollNumber);
    
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendee a WHERE a.attendanceSession.id = :sessionId AND LOWER(TRIM(a.name)) = LOWER(TRIM(:name)) AND LOWER(TRIM(a.branch)) = LOWER(TRIM(:branch)) AND LOWER(TRIM(a.year)) = LOWER(TRIM(:year))")
    boolean existsBySessionAndNameBranchYearNormalized(@Param("sessionId") UUID sessionId, @Param("name") String name, @Param("branch") String branch, @Param("year") String year);
    
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendee a WHERE a.attendanceSession.id = :sessionId AND LOWER(TRIM(a.name)) = LOWER(TRIM(:name))")
    boolean existsBySessionAndNameNormalized(@Param("sessionId") UUID sessionId, @Param("name") String name);
}
