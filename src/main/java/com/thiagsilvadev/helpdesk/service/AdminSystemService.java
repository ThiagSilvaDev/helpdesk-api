package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.AdminSystemDTO;
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
    public AdminSystemDTO.Health.Response getHealth() {
        return mapHealthDescriptor(healthEndpoint.health());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AdminSystemDTO.Metric.NamesResponse listMetricNames() {
        MetricsEndpoint.MetricNamesDescriptor descriptor = metricsEndpoint.listNames();
        List<String> names = descriptor.getNames().stream()
                .sorted()
                .toList();

        return new AdminSystemDTO.Metric.NamesResponse(names);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AdminSystemDTO.Metric.DetailResponse getMetric(String metricName) {
        MetricsEndpoint.MetricDescriptor descriptor = metricsEndpoint.metric(metricName, List.of());
        if (descriptor == null) {
            throw new NotFoundException("Metric not found with name: " + metricName);
        }

        return mapMetricDescriptor(descriptor);
    }

    private AdminSystemDTO.Health.Response mapHealthDescriptor(HealthDescriptor descriptor) {
        return new AdminSystemDTO.Health.Response(
                getStatusCode(descriptor),
                mapHealthComponents(descriptor)
        );
    }

    private List<AdminSystemDTO.Health.ComponentResponse> mapHealthComponents(HealthDescriptor descriptor) {
        if (!(descriptor instanceof CompositeHealthDescriptor compositeHealthDescriptor)) {
            return List.of();
        }

        return Objects.requireNonNull(compositeHealthDescriptor.getComponents()).entrySet().stream()
                .map(entry -> new AdminSystemDTO.Health.ComponentResponse(
                        entry.getKey(),
                        getStatusCode(entry.getValue())
                ))
                .sorted(Comparator.comparing(AdminSystemDTO.Health.ComponentResponse::name))
                .toList();
    }

    private AdminSystemDTO.Metric.DetailResponse mapMetricDescriptor(MetricsEndpoint.MetricDescriptor descriptor) {
        String description = descriptor.getDescription() != null ? descriptor.getDescription() : "";
        String baseUnit = descriptor.getBaseUnit() != null ? descriptor.getBaseUnit() : "";

        List<AdminSystemDTO.Metric.MeasurementResponse> measurements = descriptor.getMeasurements().stream()
                .map(sample -> new AdminSystemDTO.Metric.MeasurementResponse(
                        sample.getStatistic().name(),
                        sample.getValue()
                ))
                .toList();

        List<AdminSystemDTO.Metric.TagResponse> availableTags = descriptor.getAvailableTags().stream()
                .map(tag -> new AdminSystemDTO.Metric.TagResponse(
                        tag.getTag(),
                        tag.getValues().stream().sorted().toList()
                ))
                .toList();

        return new AdminSystemDTO.Metric.DetailResponse(
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
