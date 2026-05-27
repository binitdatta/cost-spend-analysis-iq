package com.enterprise.costiq.controller;

import com.enterprise.costiq.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST endpoint that streams the augmented Excel workbook.
 *
 * Called by the Flask ChatBot at GET /api/export/excel with a Keycloak
 * Bearer token. Spring Security's @Order(1) resource-server filter chain
 * validates the JWT and allows the download.
 *
 * Also accessible from the CostIQ UI dashboard via the browser directly
 * (the UI session cookie satisfies the @Order(2) UI filter chain).
 */
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExcelExportController {

    private final ExcelExportService excelExportService;

    @GetMapping("/excel")
    public ResponseEntity<byte[]> downloadExcel() throws IOException {

        byte[] workbook = excelExportService.buildWorkbook();

        String filename = "CostIQ_Report_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(workbook);
    }
}