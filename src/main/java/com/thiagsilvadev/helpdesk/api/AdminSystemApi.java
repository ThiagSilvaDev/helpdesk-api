package com.thiagsilvadev.helpdesk.api;

import com.thiagsilvadev.helpdesk.api.annotations.ApiSecurityResponseErrors;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemHealthResponse;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemMetricDetailResponse;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemMetricNamesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/api/admin/system", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Admin System", description = "Admin-only system and actuator information for frontend API generation")
@SecurityRequirement(name = "bearerAuth")
@ApiSecurityResponseErrors
public interface AdminSystemApi {

    @GetMapping("/health")
    @Operation(operationId = "getAdminSystemHealth")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Health retrieved")})
    ResponseEntity<AdminSystemHealthResponse> getAdminSystemHealth();

    @GetMapping("/metrics")
    @Operation(operationId = "listAdminSystemMetricNames")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Metric names retrieved")})
    ResponseEntity<AdminSystemMetricNamesResponse> listAdminSystemMetricNames();

    @GetMapping("/metrics/{metricName}")
    @Operation(operationId = "getAdminSystemMetric")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Metric retrieved"),
        @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<AdminSystemMetricDetailResponse> getAdminSystemMetricByName(@PathVariable String metricName);
}
