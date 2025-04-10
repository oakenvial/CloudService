package org.example.cloudservice.exception;

import org.example.cloudservice.dto.ErrorResponseDto;
import org.example.cloudservice.util.RandomIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles common exceptions that indicate a bad request.
     */
    @ExceptionHandler({
            MissingServletRequestPartException.class,
            MethodArgumentNotValidException.class,
            BindException.class,
            HttpMessageNotReadableException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponseDto> handleBadRequestExceptions(Exception e) {
        String message = "Invalid request: " + e.getMessage();
        logger.error("Bad Request encountered: {}", e.getMessage(), e);
        int errorId = RandomIdGenerator.generateRandomId();
        ErrorResponseDto errorResponse = new ErrorResponseDto(message, errorId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
