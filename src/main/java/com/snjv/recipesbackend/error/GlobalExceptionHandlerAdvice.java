package com.snjv.recipesbackend.error;

import com.snjv.recipesbackend.payload.response.ApiError;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.webjars.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        final List<Map<String, String>> errors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().parallelStream()
                .map(fieldError -> Collections.singletonMap(fieldError.getField(), fieldError.getDefaultMessage()))
                .forEach(errors::add);

        ex.getBindingResult().getGlobalErrors().parallelStream()
                .map(objectError -> Collections.singletonMap(objectError.getObjectName(), objectError.getDefaultMessage()))
                .forEach(errors::add);

        final ApiError error = new ApiError(HttpStatus.BAD_REQUEST, "Validation failed", errors);

        return handleExceptionInternal(ex, error, headers, error.getStatus(), request);
    }

    private static final Pattern ENUM_MSG = Pattern.compile("values accepted for Enum class: \\[([^]]+)\\]");

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (ex.getCause() != null && ex.getCause() instanceof InvalidFormatException) {
            Matcher match = ENUM_MSG.matcher(ex.getCause().getMessage());
            if (match.find()) {
                return new ResponseEntity(
                        ApiError.builder()
                                .message("Validation failed")
                                .status(HttpStatus.BAD_REQUEST)
                                .errors(
                                        Collections.singletonList(
                                                Collections.singletonMap(
                                                        ((InvalidFormatException) ex.getCause()).getPath().get(0).getFieldName(), "value should be: " + match.group(1)
                                                )
                                        )
                                )
                                .build(),
                        new HttpHeaders(),
                        HttpStatus.BAD_REQUEST
                );
            }
        }
        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity handleNotFoundException(final NotFoundException ex, final WebRequest request) {
        return new ResponseEntity(
                ApiError.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.BAD_REQUEST)
                        .errors(ex.getMessage())
                        .build(),
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST
        );
    }

}
