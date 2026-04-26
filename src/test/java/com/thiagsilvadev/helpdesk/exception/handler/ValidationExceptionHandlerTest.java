package com.thiagsilvadev.helpdesk.exception.handler;

import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.exception.ProblemDetailFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ValidationExceptionHandlerTest {

    private static final String NOT_BLANK_MESSAGE = "Title cannot be blank";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProblemDetailFactory problemDetails = new ProblemDetailFactory();
        mockMvc = standaloneSetup(new DummyController())
                .setControllerAdvice(new ValidationExceptionHandler(problemDetails))
                .build();
    }

    @Nested
    class MethodArgumentNotValid {
        @Test
        void shouldHandleValidationErrorsOnRequestBody() throws Exception {
            mockMvc.perform(post("/test-validation/dto")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.invalid_params[0].name").value("title"))
                    .andExpect(jsonPath("$.invalid_params[0].reason").value(NOT_BLANK_MESSAGE));
        }
    }

    @Nested
    class HttpMessageNotReadable {
        @Test
        void shouldHandleInvalidEnumValues() throws Exception {
            mockMvc.perform(post("/test-validation/enum")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"ROLE_SUPERMAN\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Malformed request body"))
                    .andExpect(jsonPath("$.invalid_params[0].name").value("role"))
                    .andExpect(jsonPath("$.invalid_params[0].reason", containsString("Accepted values are:")));
        }

        @Test
        void shouldHandleMalformedJson() throws Exception {
            mockMvc.perform(post("/test-validation/dto")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ title: "))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Malformed request body"));
        }
    }

    @Nested
    class MethodArgumentTypeMismatch {
        @Test
        void shouldHandleWrongTypeInUrl() throws Exception {
            mockMvc.perform(get("/test-validation/mismatch/text"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Type mismatch for parameter."))
                    .andExpect(jsonPath("$.invalid_params[0].name").value("id"))
                    .andExpect(jsonPath("$.invalid_params[0].reason", containsString("Long")));
        }
    }

    @Nested
    class RequestBinding {
        @Test
        void shouldHandleMissingRequestParam() throws Exception {
            mockMvc.perform(get("/test-validation/param"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Missing required request component"))
                    .andExpect(jsonPath("$.invalid_params[0].name").value("val"));
        }

        @Test
        void shouldHandleMissingRequestHeader() throws Exception {
            mockMvc.perform(get("/test-validation/header"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.invalid_params[0].name").value("X-Custom-Header"));
        }
    }

    @RestController
    @RequestMapping("/test-validation")
    static class DummyController {

        record DummyDto(@NotBlank(message = NOT_BLANK_MESSAGE) String title) {
        }

        record DummyEnumDto(Roles role) {
        }

        @PostMapping("/dto")
        void testDto(@Valid @RequestBody DummyDto dto) {
        }

        @PostMapping("/enum")
        void testEnum(@RequestBody DummyEnumDto dto) {
        }

        @GetMapping("/mismatch/{id}")
        void testMismatch(@PathVariable Long id) {
        }

        @GetMapping("/param")
        void testParam(@RequestParam @Min(1) Integer val) {
        }

        @GetMapping("/header")
        void testHeader(@RequestHeader("X-Custom-Header") @NotBlank String header) {
        }
    }
}
