package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.AdminSystemDTO;
import com.thiagsilvadev.helpdesk.service.AdminSystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AdminSystemControllerTest {

    private AdminSystemService adminSystemService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        adminSystemService = mock(AdminSystemService.class);
        mockMvc = standaloneSetup(new AdminSystemController(adminSystemService)).build();
    }

    @Test
    void shouldReturnHealth() throws Exception {
        given(adminSystemService.getHealth()).willReturn(new AdminSystemDTO.Health.Response(
                "UP",
                List.of(new AdminSystemDTO.Health.ComponentResponse("db", "UP"))
        ));

        mockMvc.perform(get("/api/admin/system/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components[0].name").value("db"));
    }

    @Test
    void shouldListMetricNames() throws Exception {
        given(adminSystemService.listMetricNames())
                .willReturn(new AdminSystemDTO.Metric.NamesResponse(List.of("jvm.memory.used", "http.server.requests")));

        mockMvc.perform(get("/api/admin/system/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names[0]").value("jvm.memory.used"));
    }

    @Test
    void shouldGetMetricByName() throws Exception {
        given(adminSystemService.getMetric("jvm.memory.used"))
                .willReturn(new AdminSystemDTO.Metric.DetailResponse(
                        "jvm.memory.used",
                        "Used memory",
                        "bytes",
                        List.of(new AdminSystemDTO.Metric.MeasurementResponse("VALUE", 42.0)),
                        List.of(new AdminSystemDTO.Metric.TagResponse("area", List.of("heap")))
                ));

        mockMvc.perform(get("/api/admin/system/metrics/jvm.memory.used"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("jvm.memory.used"))
                .andExpect(jsonPath("$.measurements[0].value").value(42.0));

        then(adminSystemService).should().getMetric("jvm.memory.used");
    }
}
