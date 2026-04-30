package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemHealthComponentResponse;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemHealthResponse;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemMetricDetailResponse;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemMetricMeasurementResponse;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemMetricNamesResponse;
import com.thiagsilvadev.helpdesk.dto.adminsystem.AdminSystemMetricTagResponse;
import com.thiagsilvadev.helpdesk.exception.ResourceNotFoundException;
import com.thiagsilvadev.helpdesk.exception.ResourceType;
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
    public AdminSystemHealthResponse getHealth() {
        return mapHealthDescriptor(healthEndpoint.health());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AdminSystemMetricNamesResponse listMetricNames() {
        MetricsEndpoint.MetricNamesDescriptor descriptor = metricsEndpoint.listNames();
        List<String> names = descriptor.getNames().stream()
                .sorted()
                .toList();

        return new AdminSystemMetricNamesResponse(names);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AdminSystemMetricDetailResponse getMetric(String metricName) {
        MetricsEndpoint.MetricDescriptor descriptor = metricsEndpoint.metric(metricName, List.of());
        if (descriptor == null) {
            throw new ResourceNotFoundException(ResourceType.METRIC, metricName);
        }

        return mapMetricDescriptor(descriptor);
    }

    private AdminSystemHealthResponse mapHealthDescriptor(HealthDescriptor descriptor) {
        return new AdminSystemHealthResponse(
                getStatusCode(descriptor),
                mapHealthComponents(descriptor)
        );
    }

    private List<AdminSystemHealthComponentResponse> mapHealthComponents(HealthDescriptor descriptor) {
        if (!(descriptor instanceof CompositeHealthDescriptor compositeHealthDescriptor)) {
            return List.of();
        }

        return Objects.requireNonNull(compositeHealthDescriptor.getComponents()).entrySet().stream()
                .map(entry -> new AdminSystemHealthComponentResponse(
                        entry.getKey(),
                        getStatusCode(entry.getValue())
                ))
                .sorted(Comparator.comparing(AdminSystemHealthComponentResponse::name))
                .toList();
    }

    private AdminSystemMetricDetailResponse mapMetricDescriptor(MetricsEndpoint.MetricDescriptor descriptor) {
        String description = descriptor.getDescription() != null ? descriptor.getDescription() : "";
        String baseUnit = descriptor.getBaseUnit() != null ? descriptor.getBaseUnit() : "";

        List<AdminSystemMetricMeasurementResponse> measurements = descriptor.getMeasurements().stream()
                .map(sample -> new AdminSystemMetricMeasurementResponse(
                        sample.getStatistic().name(),
                        sample.getValue()
                ))
                .toList();

        List<AdminSystemMetricTagResponse> availableTags = descriptor.getAvailableTags().stream()
                .map(tag -> new AdminSystemMetricTagResponse(
                        tag.getTag(),
                        tag.getValues().stream().sorted().toList()
                ))
                .toList();

        return new AdminSystemMetricDetailResponse(
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
