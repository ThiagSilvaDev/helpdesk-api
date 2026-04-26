package com.thiagsilvadev.helpdesk.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public interface AdminSystemDTO {

    interface Health {
        @Schema(description = "Health summary returned by the admin system API")
        record Response(
                @Schema(description = "Overall system health status", example = "UP")
                String status,
                @Schema(description = "Top-level health contributors returned by Spring Boot health")
                List<ComponentResponse> components
        ) {
        }

        @Schema(description = "Status of a single health contributor exposed by the admin system API")
        record ComponentResponse(
                @Schema(description = "Contributor name", example = "db")
                String name,
                @Schema(description = "Contributor status", example = "UP")
                String status
        ) {
        }
    }

    interface Metric {
        @Schema(description = "Detailed actuator metric returned by the admin system API")
        record DetailResponse(
                @Schema(description = "Metric name", example = "jvm.memory.used")
                String name,
                @Schema(description = "Metric description", example = "The amount of used memory")
                String description,
                @Schema(description = "Metric base unit", example = "bytes")
                String baseUnit,
                @Schema(description = "Metric measurements")
                List<MeasurementResponse> measurements,
                @Schema(description = "Available tag names and values")
                List<TagResponse> availableTags
        ) {
        }

        @Schema(description = "Catalog of metric names exposed by the admin system API")
        record NamesResponse(
                @Schema(description = "Sorted metric names exposed by the backend")
                List<String> names
        ) {
        }

        @Schema(description = "Single measurement sample for a metric")
        record MeasurementResponse(
                @Schema(description = "Micrometer statistic name", example = "COUNT")
                String statistic,
                @Schema(description = "Metric value", example = "42.0")
                Double value
        ) {
        }

        @Schema(description = "Tag values available for a metric")
        record TagResponse(
                @Schema(description = "Tag name", example = "uri")
                String tag,
                @Schema(description = "Available values for the tag")
                List<String> values
        ) {
        }
    }
}
