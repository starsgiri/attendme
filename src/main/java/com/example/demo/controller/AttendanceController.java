package com.example.demo.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.entity.AttendanceSession;
import com.example.demo.entity.Attendee;
import com.example.demo.repository.AttendanceSessionRepository;
import com.example.demo.repository.AttendeeRepository;
import com.example.demo.service.CSVExportService;
import com.example.demo.service.ExcelExportService;
import com.example.demo.service.GeoService;
import com.example.demo.service.QRService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AttendanceController {

    private static final double MIN_ALLOWED_RADIUS_METERS = 5.0;

    private final AttendanceSessionRepository sessionRepository;
    private final AttendeeRepository attendeeRepository;
    private final QRService qrService;
    private final GeoService geoService;
    private final ExcelExportService excelExportService;
    private final CSVExportService csvExportService;

    @GetMapping("/")
    public String showCreateSessionForm() {
        return "create-session";
    }

    @GetMapping("/history")
    public String showHistory(Model model) {
        model.addAttribute("sessions", sessionRepository.findAll());
        return "history";
    }

    @PostMapping("/session")
    public String createSession(
            @RequestParam String sessionName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String sessionType,
            @RequestParam Double targetLatitude,
            @RequestParam Double targetLongitude,
            @RequestParam Double allowedRadius,
            @RequestParam(required = false) List<String> activeFields) {

        double safeAllowedRadius = Math.max(allowedRadius, MIN_ALLOWED_RADIUS_METERS);

        AttendanceSession session = AttendanceSession.builder()
                .sessionName(sessionName)
                .department(department)
                .sessionType(sessionType)
                .targetLatitude(targetLatitude)
                .targetLongitude(targetLongitude)
                .allowedRadius(safeAllowedRadius)
                .build();

        Map<String, Boolean> fields = new HashMap<>();
        if (activeFields != null) {
            for (String field : activeFields) {
                fields.put(field, true);
            }
        }
        session.setActivatedFields(fields);

        session = sessionRepository.save(session);

        return "redirect:/session/" + session.getId() + "/publish";
    }

    @GetMapping("/session/{id}/publish")
    public String publishSession(@PathVariable UUID id, HttpServletRequest request, Model model) {
        AttendanceSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session ID"));

        List<Attendee> attendees = attendeeRepository.findByAttendanceSessionId(id);

        // Build absolute URL for the attendee form
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
        String attendeeUrl = baseUrl + "/attend/" + id;

        // Generate QR code
        String qrCodeBase64 = qrService.generateQRCodeBase64(attendeeUrl, 300, 300);

        model.addAttribute("activeSession", session);
        model.addAttribute("attendeeUrl", attendeeUrl);
        model.addAttribute("qrCodeBase64", qrCodeBase64);
        model.addAttribute("attendees", attendees);

        return "publish";
    }

    @GetMapping("/attend/{sessionId}")
    public String showAttendeeForm(@PathVariable UUID sessionId, Model model) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session ID"));

        model.addAttribute("activeSession", session);
        return "attendee-form";
    }

    @PostMapping("/attend/{sessionId}")
    public String submitAttendance(
            @PathVariable UUID sessionId,
            @ModelAttribute Attendee attendee,
            Model model) {
        
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session ID"));
        
        attendee.setAttendanceSession(session);
        
        if (attendee.getCapturedLatitude() != null && attendee.getCapturedLongitude() != null) {
            double distance = geoService.calculateDistanceInMeters(
                    session.getTargetLatitude(), session.getTargetLongitude(),
                    attendee.getCapturedLatitude(), attendee.getCapturedLongitude()
            );
            attendee.setWithinRange(distance <= session.getAllowedRadius());
        } else {
            attendee.setWithinRange(false);
        }

        attendeeRepository.save(attendee);
        model.addAttribute("successMessage", "Attendance marked successfully!");
        model.addAttribute("isWithinRange", attendee.isWithinRange());
        return "success";
    }

    @GetMapping("/session/{id}/dashboard")
    public String showDashboard(@PathVariable UUID id, HttpServletRequest request, Model model) {
        AttendanceSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session ID"));

        List<Attendee> attendees = attendeeRepository.findByAttendanceSessionId(id);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
        String attendeeUrl = baseUrl + "/attend/" + id;
        String qrCodeBase64 = qrService.generateQRCodeBase64(attendeeUrl, 300, 300);

        model.addAttribute("activeSession", session);
        model.addAttribute("attendees", attendees);
        model.addAttribute("qrCodeBase64", qrCodeBase64);
        model.addAttribute("attendeeUrl", attendeeUrl);

        return "admin-dashboard";
    }

    @ResponseBody
    @GetMapping("/session/{id}/attendees-data")
    public List<Attendee> getAttendeesData(@PathVariable UUID id) {
        return attendeeRepository.findByAttendanceSessionId(id);
    }

    @GetMapping("/session/{id}/export-csv")
    public void exportCSV(@PathVariable UUID id, HttpServletResponse response) throws IOException {
        AttendanceSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session ID"));

        List<Attendee> attendees = attendeeRepository.findByAttendanceSessionId(id);

        response.setContentType("text/csv");
        String filename = "attendance_" + session.getSessionName().replaceAll("\\s+", "_") + ".csv";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        csvExportService.exportAttendeesToCSV(response.getWriter(), session, attendees);
    }

    @GetMapping(value = "/session/{id}/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<InputStreamResource> exportAttendanceExcel(@PathVariable UUID id) {
        AttendanceSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session ID"));

        List<Attendee> attendees = attendeeRepository.findByAttendanceSessionId(id);
        
        ByteArrayInputStream in = excelExportService.exportAttendeesToExcel(session, attendees);

        HttpHeaders headers = new HttpHeaders();
        // Sets dynamic filename
        String filename = "Attendance_" + session.getSessionName().replaceAll("\\s+", "_") + ".xlsx";
        headers.add("Content-Disposition", "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(in));
    }
}
