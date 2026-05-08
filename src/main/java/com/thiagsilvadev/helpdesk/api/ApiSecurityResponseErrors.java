package com.thiagsilvadev.helpdesk.api;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "401", ref = "Unauthorized"),
        @ApiResponse(responseCode = "403", ref = "Forbidden")
})
public @interface ApiSecurityResponseErrors {
}
