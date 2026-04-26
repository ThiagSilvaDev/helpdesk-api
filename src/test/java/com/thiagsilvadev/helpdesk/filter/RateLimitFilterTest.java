package com.thiagsilvadev.helpdesk.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class RateLimitFilterTest {

    @Test
    void shouldSkipNonAuthApiRequests() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(new JsonMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.setRemoteAddr("192.0.2.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        then(chain).should().doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void shouldRejectAuthRequestsAfterLimitIsExceeded() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(new JsonMapper());
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest allowedRequest = new MockHttpServletRequest("POST", "/api/auth/login");
            allowedRequest.setRemoteAddr("192.0.2.10");
            filter.doFilter(allowedRequest, new MockHttpServletResponse(), chain);
        }

        MockHttpServletRequest rejectedRequest = new MockHttpServletRequest("POST", "/api/auth/login");
        rejectedRequest.setRemoteAddr("192.0.2.10");
        MockHttpServletResponse rejectedResponse = new MockHttpServletResponse();

        filter.doFilter(rejectedRequest, rejectedResponse, chain);

        assertThat(rejectedResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(rejectedResponse.getHeader("Retry-After")).isNotBlank();
        assertThat(rejectedResponse.getContentAsString()).contains("Rate limit exceeded");
    }

    @Test
    void shouldUseFirstForwardedIpAsBucketKey() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(new JsonMapper());
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest allowedRequest = new MockHttpServletRequest("POST", "/api/auth/login");
            allowedRequest.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.5");
            filter.doFilter(allowedRequest, new MockHttpServletResponse(), chain);
        }

        MockHttpServletRequest rejectedRequest = new MockHttpServletRequest("POST", "/api/auth/login");
        rejectedRequest.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.6");
        MockHttpServletResponse rejectedResponse = new MockHttpServletResponse();

        filter.doFilter(rejectedRequest, rejectedResponse, chain);

        assertThat(rejectedResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }
}
