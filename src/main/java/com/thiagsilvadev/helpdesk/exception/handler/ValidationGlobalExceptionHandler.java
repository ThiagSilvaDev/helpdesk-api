package com.thiagsilvadev.helpdesk.exception.handler;

import com.thiagsilvadev.helpdesk.exception.GlobalExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(2)
public class ValidationGlobalExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();

        return enrichProblemDetail(ex.getBody(), errors);
    }

    private String toFieldError(FieldError error) {
        String message = error.getDefaultMessage() != null ? error.getDefaultMessage() : "invalid value";
        return error.getField() + ": " + message;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return createProblemDetail(HttpStatus.BAD_REQUEST, ex.getName() + " should be a valid type");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException cause && cause.getTargetType().isEnum()) {
            String accepted = Arrays.stream(cause.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            String field = cause.getPath() != null && !cause.getPath().isEmpty()
                    ? cause.getPath().getLast().getPropertyName()
                    : "unknown";

            String message = "Invalid value '%s' for field '%s'. Accepted values: [%s]"
                    .formatted(cause.getValue(), field, accepted);
            return createProblemDetail(HttpStatus.BAD_REQUEST, message);
        }

        return createProblemDetail(HttpStatus.BAD_REQUEST, "Malformed request body");
    }
}
