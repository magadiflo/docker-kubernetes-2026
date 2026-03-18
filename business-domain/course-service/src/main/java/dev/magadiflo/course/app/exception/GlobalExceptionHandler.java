package dev.magadiflo.course.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(CourseNotFoundException ex, HttpServletRequest request) {
        log.error("Curso no encontrado: {}", ex.getMessage());
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

    /**
     * Manejo de usuario inexistente en el servicio remoto.
     * Se dispara cuando el user-service devuelve un 404 explícito.
     */
    @ExceptionHandler(RemoteUserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRemoteUserNotFoundException(RemoteUserNotFoundException ex, HttpServletRequest request) {
        log.error("Usuario no encontrado en el [user-service]: {}", ex.getMessage());
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

    /**
     * Manejo de respuestas HTTP de error (4xx y 5xx) del RestClient.
     * Mapeado como BAD_GATEWAY para indicar que un servidor intermedio falló.
     */
    @ExceptionHandler(value = {
            HttpClientErrorException.class, // (errores HTTP 4xx)
            HttpServerErrorException.class  // (errores HTTP 5xx)
    })
    public ResponseEntity<ErrorResponse> handleRestClientResponseException(RestClientResponseException ex, HttpServletRequest request) {
        log.warn("Error del servicio remoto [user-service] - status: {} - body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        var errorResponse = this.buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(errorResponse);
    }

    /**
     * Manejo de fallos críticos de conectividad.
     * Captura caídas del servicio, timeouts o errores de resolución de DNS.
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(RestClientException ex, HttpServletRequest request) {
        log.error("Fallo de red o fallo del tiempo de espera al comunicarse con el servicio remoto [user-service]", ex);
        var errorResponse = this.buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "No se pudo comunicar con el servicio remoto. " + ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
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
