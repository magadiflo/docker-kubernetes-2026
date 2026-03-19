package dev.magadiflo.course.app.exception;

/**
 * Excepción lanzada cuando un ID de usuario es válido sintácticamente
 * pero no existe en la base de datos maestra del user-service.
 */
public class RemoteUserNotFoundException extends RuntimeException {
    public RemoteUserNotFoundException(Long userId) {
        super("El usuario con id [%d] no fue encontrado en el [user-service]".formatted(userId));
    }
}
