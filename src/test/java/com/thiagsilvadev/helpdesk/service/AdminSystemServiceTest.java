package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.AdminSystemDTO;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AdminSystemServiceTest {

    private static final String METRIC_NAME = "jvm.memory.used";
    private static final String MISSING_METRIC = "nonexistent.metric";

    @Mock
    private HealthEndpoint healthEndpoint;

    @Mock
    private MetricsEndpoint metricsEndpoint;

    @InjectMocks
    private AdminSystemService adminSystemService;

    @Nested
    class ListMetricNames {

        @Test
        void shouldReturnSortedMetricNames() {
            MetricsEndpoint.MetricNamesDescriptor descriptor = mock(MetricsEndpoint.MetricNamesDescriptor.class);
            given(descriptor.getNames()).willReturn(Set.of("jvm.memory.max", "jvm.memory.used", "http.requests"));
            given(metricsEndpoint.listNames()).willReturn(descriptor);

            AdminSystemDTO.Metric.NamesResponse response = adminSystemService.listMetricNames();

            assertThat(response.names())
                    .containsExactly("http.requests", "jvm.memory.max", "jvm.memory.used");
        }

        @Test
        void shouldReturnEmptyListWhenNoMetrics() {
            MetricsEndpoint.MetricNamesDescriptor descriptor = mock(MetricsEndpoint.MetricNamesDescriptor.class);
            given(descriptor.getNames()).willReturn(Set.of());
            given(metricsEndpoint.listNames()).willReturn(descriptor);

            AdminSystemDTO.Metric.NamesResponse response = adminSystemService.listMetricNames();

            assertThat(response.names()).isEmpty();
        }

        @Test
        void shouldHandleSingleMetric() {
            MetricsEndpoint.MetricNamesDescriptor descriptor = mock(MetricsEndpoint.MetricNamesDescriptor.class);
            given(descriptor.getNames()).willReturn(Set.of("single.metric"));
            given(metricsEndpoint.listNames()).willReturn(descriptor);

            AdminSystemDTO.Metric.NamesResponse response = adminSystemService.listMetricNames();

            assertThat(response.names()).containsExactly("single.metric");
        }
    }

    @Nested
    class GetMetric {

        @Test
        void shouldReturnMetricDetail() {
            MetricsEndpoint.MetricDescriptor descriptor = mock(MetricsEndpoint.MetricDescriptor.class);
            given(descriptor.getName()).willReturn(METRIC_NAME);
            given(descriptor.getDescription()).willReturn("Memory usage of JVM");
            given(descriptor.getBaseUnit()).willReturn("bytes");
            given(descriptor.getMeasurements()).willReturn(List.of());
            given(descriptor.getAvailableTags()).willReturn(List.of());
            given(metricsEndpoint.metric(METRIC_NAME, List.of())).willReturn(descriptor);

            AdminSystemDTO.Metric.DetailResponse response = adminSystemService.getMetric(METRIC_NAME);

            assertThat(response.name()).isEqualTo(METRIC_NAME);
            assertThat(response.description()).isEqualTo("Memory usage of JVM");
            assertThat(response.baseUnit()).isEqualTo("bytes");
        }

        @Test
        void shouldThrowWhenMetricNotFound() {
            given(metricsEndpoint.metric(MISSING_METRIC, List.of())).willReturn(null);

            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> adminSystemService.getMetric(MISSING_METRIC))
                    .withMessage("Metric not found with name: " + MISSING_METRIC);
        }

        @Test
        void shouldHandleNullDescriptionAndBaseUnit() {
            MetricsEndpoint.MetricDescriptor descriptor = mock(MetricsEndpoint.MetricDescriptor.class);
            given(descriptor.getName()).willReturn(METRIC_NAME);
            given(descriptor.getDescription()).willReturn(null);
            given(descriptor.getBaseUnit()).willReturn(null);
            given(descriptor.getMeasurements()).willReturn(List.of());
            given(descriptor.getAvailableTags()).willReturn(List.of());
            given(metricsEndpoint.metric(METRIC_NAME, List.of())).willReturn(descriptor);

            AdminSystemDTO.Metric.DetailResponse response = adminSystemService.getMetric(METRIC_NAME);

            assertThat(response.description()).isEmpty();
            assertThat(response.baseUnit()).isEmpty();
        }

        @Test
        void shouldHandleEmptyMeasurements() {
            MetricsEndpoint.MetricDescriptor descriptor = mock(MetricsEndpoint.MetricDescriptor.class);
            given(descriptor.getName()).willReturn(METRIC_NAME);
            given(descriptor.getDescription()).willReturn("");
            given(descriptor.getBaseUnit()).willReturn("");
            given(descriptor.getMeasurements()).willReturn(List.of());
            given(descriptor.getAvailableTags()).willReturn(List.of());
            given(metricsEndpoint.metric(METRIC_NAME, List.of())).willReturn(descriptor);

            AdminSystemDTO.Metric.DetailResponse response = adminSystemService.getMetric(METRIC_NAME);

            assertThat(response.measurements()).isEmpty();
        }
    }
}
