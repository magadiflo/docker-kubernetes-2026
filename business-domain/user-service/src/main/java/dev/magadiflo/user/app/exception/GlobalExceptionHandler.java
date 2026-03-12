package dev.magadiflo.user.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        log.error("Usuario no encontrado: {}", ex.getMessage());
        var errorResponse = this.buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, HttpServletRequest request) {
        log.error("El email ya existe: {}", ex.getMessage());
        var errorResponse = this.buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Error en argumentos: {}", ex.getMessage());

        Map<String, List<String>> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String field = fieldError.getField();
            String defaultMessage = fieldError.getDefaultMessage();
            errors.computeIfAbsent(field, k -> new ArrayList<>()).add(defaultMessage);
        });

        var errorResponse = this.buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Falló la validación en los campos",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception, HttpServletRequest request) {
        log.error("Error genérico: {}", exception.getMessage());
        var errorResponse = this.buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    private ErrorResponse buildErrorResponse(HttpStatus httpStatus, String message, String requestURI, Map<String, List<String>> errors) {
        return new ErrorResponse(
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                message,
                requestURI,
                errors
        );
    }
}
