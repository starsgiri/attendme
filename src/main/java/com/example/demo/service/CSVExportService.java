package com.example.demo.service;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.entity.AttendanceSession;
import com.example.demo.entity.Attendee;

@Service
public class CSVExportService {

    private final GeoService geoService;

    public CSVExportService(GeoService geoService) {
        this.geoService = geoService;
    }

    public void exportAttendeesToCSV(PrintWriter writer, AttendanceSession session, List<Attendee> attendees) {
        Map<String, Boolean> activeFields = session.getActivatedFields();

        // Print header
        StringBuilder header = new StringBuilder();
        if (activeFields.containsKey("Name")) header.append("Name,");
        if (activeFields.containsKey("RollNumber")) header.append("Roll Number/SRN,");
        if (activeFields.containsKey("Branch")) header.append("Branch,");
        if (activeFields.containsKey("Year")) header.append("Year,");
        
        header.append("Captured Lat,Captured Lng,Distance from Creator (m),Status,Timestamp");
        writer.println(header.toString());

        // Print data rows
        for (Attendee attendee : attendees) {
            StringBuilder row = new StringBuilder();
            if (activeFields.containsKey("Name")) row.append(escapeCSV(attendee.getName())).append(",");
            if (activeFields.containsKey("RollNumber")) row.append(escapeCSV(attendee.getRollNumber())).append(",");
            if (activeFields.containsKey("Branch")) row.append(escapeCSV(attendee.getBranch())).append(",");
            if (activeFields.containsKey("Year")) row.append(escapeCSV(attendee.getYear())).append(",");

            if (attendee.getCapturedLatitude() != null && attendee.getCapturedLongitude() != null) {
                row.append(attendee.getCapturedLatitude()).append(",");
                row.append(attendee.getCapturedLongitude()).append(",");
                
                double distance = geoService.calculateDistanceInMeters(
                        session.getTargetLatitude(), session.getTargetLongitude(),
                        attendee.getCapturedLatitude(), attendee.getCapturedLongitude()
                );
                row.append(String.format("%.2f", distance)).append(",");
            } else {
                row.append(",,N/A,");
            }
            
            row.append(attendee.isWithinRange() ? "Valid" : "Invalid").append(",");
            row.append(attendee.getMarkedAt() != null ? attendee.getMarkedAt().toString() : "");
            
            writer.println(row.toString());
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
