package dev.magadiflo.course.app.client;

import dev.magadiflo.course.app.dto.UserRequest;
import dev.magadiflo.course.app.dto.UserResponse;
import dev.magadiflo.course.app.exception.RemoteUserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserServiceClient {

    private final RestClient restClient;

    /**
     * Recupera un usuario del microservicio remoto.
     * <p>
     * Se utiliza .exchange() para interceptar el flujo de respuesta y realizar un
     * mapeo de excepciones basado en el código de estado HTTP (HttpStatusCode).
     *
     * @param userId Identificador único del usuario en el servicio central (user-service).
     * @return UserResponse DTO con la información del usuario.
     * @throws RemoteUserNotFoundException Si el servicio remoto devuelve un 404.
     * @throws RestClientResponseException Generada por {@code clientResponse.createException()}
     *                                     para cualquier otro error 4xx o 5xx proveniente del servidor.
     */
    public UserResponse getUserFromUserService(Long userId) {
        log.info("Consultando el servicio [user-service] por el usuario con id: {}", userId);

        UserResponse userResponse = this.restClient
                .get()
                .uri("/{userId}", userId)
                .exchange((clientRequest, clientResponse) -> {
                    HttpStatusCode statusCode = clientResponse.getStatusCode();

                    // 1. Éxito (2xx): Deserialización directa del cuerpo de la respuesta.
                    if (statusCode.is2xxSuccessful()) {
                        return clientResponse.bodyTo(UserResponse.class);
                    }

                    // 2. Negocio (404): El usuario no existe en el sistema maestro.
                    // Lanzamos una excepción personalizada para un manejo semántico en el CourseService.
                    if (statusCode == HttpStatus.NOT_FOUND) {
                        log.warn("El usuario con id [{}] no existe en el servicio remoto", userId);
                        throw new RemoteUserNotFoundException(userId);
                    }

                    log.error("Error no controlado. Status: {}", statusCode);
                    // 3. Infraestructura (Otros 4xx, 5xx): Delegación al framework.
                    // clientResponse.createException() genera una excepción que encapsula el body,
                    // los headers y el status real, permitiendo que el GlobalExceptionHandler
                    // la capture como una RestClientResponseException.
                    throw clientResponse.createException();
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

    /**
     * Recupera una colección de usuarios del servicio remoto mediante una lista de IDs.
     * <p>
     * Se utiliza ParameterizedTypeReference para capturar la información de tipo
     * genérico (List<UserResponse>) durante la deserialización del cuerpo de la respuesta.
     *
     * @param userIds Lista de identificadores únicos de usuario.
     * @return Lista de objetos UserResponse con la información detallada.
     */
    public List<UserResponse> getUsersFromUserService(List<Long> userIds) {
        log.info("Iniciando recuperación masiva en [user-service] para IDs: {}", userIds);

        List<UserResponse> users = this.restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/by-ids")
                        .queryParam("userIds", userIds)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        log.info("Recuperación exitosa de usuarios en [user-service]: {}", users);
        return users;
    }
}
