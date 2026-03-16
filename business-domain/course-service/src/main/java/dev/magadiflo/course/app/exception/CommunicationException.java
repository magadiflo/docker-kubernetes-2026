package dev.magadiflo.course.app.exception;

/**
 * Excepción lanzada cuando ocurre un fallo técnico, de red, o una
 * validación fallida en el microservicio remoto.
 */
public class CommunicationException extends RuntimeException {
    public CommunicationException(String message) {
        super("Se produjo un error en el [user-service]: %s".formatted(message));
    }
}
