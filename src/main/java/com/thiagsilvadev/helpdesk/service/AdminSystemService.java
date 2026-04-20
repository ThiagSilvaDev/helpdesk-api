package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.AdminSystemDto;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class AdminSystemService {

    private final HealthEndpoint healthEndpoint;
    private final MetricsEndpoint metricsEndpoint;

    public AdminSystemService(HealthEndpoint healthEndpoint, MetricsEndpoint metricsEndpoint) {
        this.healthEndpoint = healthEndpoint;
        this.metricsEndpoint = metricsEndpoint;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AdminSystemDto.AdminSystemHealthResponse getHealth() {
        return mapHealthDescriptor(healthEndpoint.health());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AdminSystemDto.AdminSystemMetricNamesResponse listMetricNames() {
        MetricsEndpoint.MetricNamesDescriptor descriptor = metricsEndpoint.listNames();
        List<String> names = descriptor.getNames().stream()
                .sorted()
                .toList();

        return new AdminSystemDto.AdminSystemMetricNamesResponse(names);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AdminSystemDto.AdminSystemMetricResponse getMetric(String metricName) {
        MetricsEndpoint.MetricDescriptor descriptor = metricsEndpoint.metric(metricName, List.of());
        if (descriptor == null) {
            throw new NotFoundException("Metric not found with name: " + metricName);
        }

        return mapMetricDescriptor(descriptor);
    }

    private AdminSystemDto.AdminSystemHealthResponse mapHealthDescriptor(HealthDescriptor descriptor) {
        return new AdminSystemDto.AdminSystemHealthResponse(
                getStatusCode(descriptor),
                mapHealthComponents(descriptor)
        );
    }

    private List<AdminSystemDto.AdminSystemHealthComponentResponse> mapHealthComponents(HealthDescriptor descriptor) {
        if (!(descriptor instanceof CompositeHealthDescriptor compositeHealthDescriptor)) {
            return List.of();
        }

        return Objects.requireNonNull(compositeHealthDescriptor.getComponents()).entrySet().stream()
                .map(entry -> new AdminSystemDto.AdminSystemHealthComponentResponse(
                        entry.getKey(),
                        getStatusCode(entry.getValue())
                ))
                .sorted(Comparator.comparing(AdminSystemDto.AdminSystemHealthComponentResponse::name))
                .toList();
    }

    private AdminSystemDto.AdminSystemMetricResponse mapMetricDescriptor(MetricsEndpoint.MetricDescriptor descriptor) {
        String description = descriptor.getDescription() != null ? descriptor.getDescription() : "";
        String baseUnit = descriptor.getBaseUnit() != null ? descriptor.getBaseUnit() : "";

        List<AdminSystemDto.AdminSystemMetricMeasurementResponse> measurements = descriptor.getMeasurements().stream()
                .map(sample -> new AdminSystemDto.AdminSystemMetricMeasurementResponse(
                        sample.getStatistic().name(),
                        sample.getValue()
                ))
                .toList();

        List<AdminSystemDto.AdminSystemMetricTagResponse> availableTags = descriptor.getAvailableTags().stream()
                .map(tag -> new AdminSystemDto.AdminSystemMetricTagResponse(
                        tag.getTag(),
                        tag.getValues().stream().sorted().toList()
                ))
                .toList();

        return new AdminSystemDto.AdminSystemMetricResponse(
                descriptor.getName(),
                description,
                baseUnit,
                measurements,
                availableTags
        );
    }

    private String getStatusCode(HealthDescriptor descriptor) {
        descriptor.getStatus();
        return descriptor.getStatus().getCode();
    }
}
