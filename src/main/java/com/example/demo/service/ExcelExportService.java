package com.example.demo.service;

import com.example.demo.entity.AttendanceSession;
import com.example.demo.entity.Attendee;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    private final GeoService geoService;

    public ExcelExportService(GeoService geoService) {
        this.geoService = geoService;
    }

    public ByteArrayInputStream exportAttendeesToExcel(AttendanceSession session, List<Attendee> attendees) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Attendance Log");

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            int colIdx = 0;
            
            Map<String, Boolean> activeFields = session.getActivatedFields();
            if (activeFields.containsKey("Name")) headerRow.createCell(colIdx++).setCellValue("Name");
            if (activeFields.containsKey("RollNumber")) headerRow.createCell(colIdx++).setCellValue("Roll Number");
            if (activeFields.containsKey("Branch")) headerRow.createCell(colIdx++).setCellValue("Branch");
            if (activeFields.containsKey("Year")) headerRow.createCell(colIdx++).setCellValue("Year");
            
            headerRow.createCell(colIdx++).setCellValue("Captured Lat");
            headerRow.createCell(colIdx++).setCellValue("Captured Lng");
            headerRow.createCell(colIdx++).setCellValue("Distance from Creator (m)");
            headerRow.createCell(colIdx++).setCellValue("Status");
            headerRow.createCell(colIdx++).setCellValue("Timestamp");

            // Style Header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < colIdx; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            // Write Data Rows
            int rowIdx = 1;
            for (Attendee attendee : attendees) {
                Row row = sheet.createRow(rowIdx++);
                colIdx = 0;
                
                if (activeFields.containsKey("Name")) row.createCell(colIdx++).setCellValue(attendee.getName() != null ? attendee.getName() : "");
                if (activeFields.containsKey("RollNumber")) row.createCell(colIdx++).setCellValue(attendee.getRollNumber() != null ? attendee.getRollNumber() : "");
                if (activeFields.containsKey("Branch")) row.createCell(colIdx++).setCellValue(attendee.getBranch() != null ? attendee.getBranch() : "");
                if (activeFields.containsKey("Year")) row.createCell(colIdx++).setCellValue(attendee.getYear() != null ? attendee.getYear() : "");
                
                if (attendee.getCapturedLatitude() != null && attendee.getCapturedLongitude() != null) {
                    row.createCell(colIdx++).setCellValue(attendee.getCapturedLatitude());
                    row.createCell(colIdx++).setCellValue(attendee.getCapturedLongitude());
                    
                    double distance = geoService.calculateDistanceInMeters(
                            session.getTargetLatitude(), session.getTargetLongitude(),
                            attendee.getCapturedLatitude(), attendee.getCapturedLongitude()
                    );
                    row.createCell(colIdx++).setCellValue(Math.round(distance * 100.0) / 100.0);
                } else {
                    row.createCell(colIdx++).setCellValue("");
                    row.createCell(colIdx++).setCellValue("");
                    row.createCell(colIdx++).setCellValue("N/A");
                }
                
                row.createCell(colIdx++).setCellValue(attendee.isWithinRange() ? "Valid" : "Invalid");
                row.createCell(colIdx++).setCellValue(attendee.getMarkedAt() != null ? attendee.getMarkedAt().toString() : "");
            }

            // Auto-size columns
            for (int i = 0; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage());
        }
    }
}
