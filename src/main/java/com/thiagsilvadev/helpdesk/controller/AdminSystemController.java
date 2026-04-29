package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.api.AdminSystemApi;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemHealthResponse;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemMetricDetailResponse;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemMetricNamesResponse;
import com.thiagsilvadev.helpdesk.service.AdminSystemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminSystemController implements AdminSystemApi {

    private final AdminSystemService adminSystemService;

    public AdminSystemController(AdminSystemService adminSystemService) {
        this.adminSystemService = adminSystemService;
    }

    @Override
    public ResponseEntity<AdminSystemHealthResponse> getAdminSystemHealth() {
        return ResponseEntity.ok(adminSystemService.getHealth());
    }

    @Override
    public ResponseEntity<AdminSystemMetricNamesResponse> listAdminSystemMetricNames() {
        return ResponseEntity.ok(adminSystemService.listMetricNames());
    }

    @Override
    public ResponseEntity<AdminSystemMetricDetailResponse> getAdminSystemMetricByName(
            @PathVariable String metricName
    ) {
        return ResponseEntity.ok(adminSystemService.getMetric(metricName));
    }
}
