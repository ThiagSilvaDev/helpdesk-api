package com.thiagsilvadev.helpdesk.api;

import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemHealthResponse;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemMetricDetailResponse;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemMetricNamesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/admin/system")
@Tag(name = "Admin System", description = "Admin-only system and actuator information for frontend API generation")
@SecurityRequirement(name = "bearerAuth")
public interface AdminSystemApi {

    @GetMapping("/health")
    @Operation(
            operationId = "getAdminSystemHealth",
            summary = "Get system health",
            description = "Returns the Spring Boot health endpoint adapted to a stable response DTO for Angular API generation"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Health retrieved",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdminSystemHealthResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden")
    })
    ResponseEntity<AdminSystemHealthResponse> getAdminSystemHealth();

    @GetMapping("/metrics")
    @Operation(
            operationId = "listAdminSystemMetricNames",
            summary = "List system metrics",
            description = "Returns the metric names exposed by Spring Boot actuator using a dedicated DTO for Angular API generation"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Metric names retrieved",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdminSystemMetricNamesResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden")
    })
    ResponseEntity<AdminSystemMetricNamesResponse> listAdminSystemMetricNames();

    @GetMapping("/metrics/{metricName}")
    @Operation(
            operationId = "getAdminSystemMetric",
            summary = "Get system metric details",
            description = "Returns a single actuator metric with explicit DTOs so Angular API generation produces stable models"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Metric retrieved",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdminSystemMetricDetailResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<AdminSystemMetricDetailResponse> getAdminSystemMetricByName(
            @Parameter(description = "Metric name exposed by actuator", example = "jvm.memory.used")
            @PathVariable String metricName
    );
}
