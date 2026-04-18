package com.thiagsilvadev.helpdesk.exception.handler;

import com.thiagsilvadev.helpdesk.exception.GlobalExceptionHandler;
import com.thiagsilvadev.helpdesk.exception.InvalidParam;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(1)
public class ValidationGlobalExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<InvalidParam> invalidParams = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new InvalidParam(error.getField(), error.getDefaultMessage()))
                .toList();

        return enrichProblemDetail(ex.getBody(), invalidParams);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();

        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "valid type";

        String errorMessage = String.format("must be of type %s", expectedType);

        List<InvalidParam> invalidParams = List.of(new InvalidParam(paramName, errorMessage));

        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, "Type mismatch for parameter.");

        return enrichProblemDetail(problemDetail, invalidParams);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof MismatchedInputException cause) {
            String name = cause.getPath() != null && !cause.getPath().isEmpty()
                    ? cause.getPath().getLast().getPropertyName()
                    : "unknown";

            String reason;
            if (cause instanceof InvalidFormatException formatException && formatException.getTargetType().isEnum()) {
                String accepted = Arrays.stream(formatException.getTargetType().getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                reason = "Accepted values are: " + accepted;
            } else {
                String targetType = cause.getTargetType() != null
                        ? cause.getTargetType().getSimpleName()
                        : "valid type";
                reason = String.format("must be of type %s", targetType);
            }

            List<InvalidParam> invalidParams = List.of(new InvalidParam(name, reason));
            ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, "Malformed request body");
            return enrichProblemDetail(problemDetail, invalidParams);
        }

        return createProblemDetail(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(jakarta.validation.ConstraintViolationException ex) {
        List<InvalidParam> invalidParams = ex.getConstraintViolations().stream()
                .map(error -> new InvalidParam(error.getPropertyPath().toString(), error.getMessage()))
                .toList();

        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, "Constraint violation");
        return enrichProblemDetail(problemDetail, invalidParams);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ProblemDetail handleServletRequestBinding(ServletRequestBindingException ex) {
        String name = "unknown";
        String reason = ex.getMessage();

        if (ex instanceof MissingServletRequestParameterException e) {
            name = e.getParameterName();
            reason = String.format("Parameter '%s' is mandatory", name);
        } else if (ex instanceof MissingRequestHeaderException e) {
            name = e.getHeaderName();
            reason = String.format("Header '%s' is mandatory", name);
        } else if (ex instanceof MissingRequestCookieException e) {
            name = e.getCookieName();
            reason = String.format("Cookie '%s' is mandatory", name);
        }

        List<InvalidParam> invalidParams = List.of(new InvalidParam(name, reason));
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, "Missing required request component");
        return enrichProblemDetail(problemDetail, invalidParams);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        List<InvalidParam> invalidParams = new ArrayList<>();
        ex.visitResults(new HandlerMethodValidationException.Visitor() {

            private void extractSimpleErrors(@NonNull ParameterValidationResult result) {
                String paramName = Objects.requireNonNullElse(
                        result.getMethodParameter().getParameterName(),
                        "unknown"
                );

                result.getResolvableErrors().forEach(error ->
                        invalidParams.add(new InvalidParam(paramName, getErrorMessage(error)))
                );
            }

            private void extractComplexErrors(@NonNull ParameterErrors errors) {
                errors.getFieldErrors().forEach(fieldError ->
                        invalidParams.add(new InvalidParam(fieldError.getField(), getErrorMessage(fieldError)))
                );

                errors.getGlobalErrors().forEach(globalError -> {
                    String paramName = Objects.requireNonNullElse(
                            errors.getMethodParameter().getParameterName(),
                            "unknown"
                    );
                    invalidParams.add(new InvalidParam(paramName, getErrorMessage(globalError)));
                });
            }

            private String getErrorMessage(MessageSourceResolvable error) {
                return error.getDefaultMessage() != null ? error.getDefaultMessage() : "invalid value";
            }

            @Override
            public void cookieValue(@NonNull CookieValue cookieValue, @NonNull ParameterValidationResult result) {
                extractSimpleErrors(result);
            }

            @Override
            public void matrixVariable(@NonNull MatrixVariable matrixVariable, @NonNull ParameterValidationResult result) {
                extractSimpleErrors(result);
            }

            @Override
            public void modelAttribute(@Nullable ModelAttribute modelAttribute, @NonNull ParameterErrors errors) {
                extractComplexErrors(errors);
            }

            @Override
            public void pathVariable(@NonNull PathVariable pathVariable, @NonNull ParameterValidationResult result) {
                extractSimpleErrors(result);
            }

            @Override
            public void requestBody(@NonNull RequestBody requestBody, @NonNull ParameterErrors errors) {
                extractComplexErrors(errors);
            }

            @Override
            public void requestHeader(@NonNull RequestHeader requestHeader, @NonNull ParameterValidationResult result) {
                extractSimpleErrors(result);
            }

            @Override
            public void requestParam(@Nullable RequestParam requestParam, @NonNull ParameterValidationResult result) {
                extractSimpleErrors(result);
            }

            @Override
            public void requestPart(@NonNull RequestPart requestPart, @NonNull ParameterErrors errors) {
                extractComplexErrors(errors);
            }

            @Override
            public void other(@NonNull ParameterValidationResult result) {
                extractSimpleErrors(result);
            }
        });
        return enrichProblemDetail(ex.getBody(), invalidParams);
    }
}
