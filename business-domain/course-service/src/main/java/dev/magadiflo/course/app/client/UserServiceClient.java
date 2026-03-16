package dev.magadiflo.course.app.client;

import dev.magadiflo.course.app.dto.UserRequest;
import dev.magadiflo.course.app.dto.UserResponse;
import dev.magadiflo.course.app.exception.CommunicationException;
import dev.magadiflo.course.app.exception.ErrorResponse;
import dev.magadiflo.course.app.exception.RemoteUserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserServiceClient {

    private final RestClient restClient;

    /**
     * 🔍 Recupera un usuario específico del microservicio remoto.
     * Utiliza .exchange() para un control granular sobre los códigos de estado HTTP.
     */
    public UserResponse getUserFromUserService(Long userId) {
        log.info("Consultando el servicio [user-service] por el usuario con id: {}", userId);

        UserResponse userResponse = this.restClient
                .get()
                .uri("/{userId}", userId)
                .exchange((clientRequest, clientResponse) -> {
                    HttpStatusCode statusCode = clientResponse.getStatusCode();
                    if (statusCode == HttpStatus.OK) {
                        return clientResponse.bodyTo(UserResponse.class);
                    }

                    if (statusCode == HttpStatus.NOT_FOUND) {
                        throw new RemoteUserNotFoundException(userId);
                    }

                    // Captura de errores estructurados provenientes del GlobalExceptionHandler remoto
                    ErrorResponse errorResponse = clientResponse.bodyTo(ErrorResponse.class);
                    String message = Optional.ofNullable(errorResponse)
                            .map(ErrorResponse::error)
                            .orElseGet(() -> "Error desconocido al consultar el user-service");

                    log.info("Mensaje de error desde el user-service: {}", message);
                    throw new CommunicationException(message);
                });

        log.info("El servicio [user-service] encontró al usuario buscado: {}", userResponse);
        return userResponse;
    }

    /**
     * 🆕 Registra un nuevo usuario delegando la persistencia al user-service.
     */
    public UserResponse createUserInUserService(UserRequest userRequest) {
        log.info("Usuario a registrar en el [user-service]: {}", userRequest);

        UserResponse userResponse = this.restClient
                .post()
                .body(userRequest)
                .retrieve()
                .body(UserResponse.class);

        log.info("Usuario registrado con éxito en el [user-service]: {}", userResponse);
        return userResponse;
    }
}
