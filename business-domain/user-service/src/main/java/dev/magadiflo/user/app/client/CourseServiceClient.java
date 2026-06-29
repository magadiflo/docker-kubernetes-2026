package dev.magadiflo.user.app.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class CourseServiceClient {

    private static final String COURSE_URI = "/api/v1/courses";
    private final RestClient restClient;

    public CourseServiceClient(@Qualifier("courseRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Notifica al course-service para eliminar las asociaciones de un usuario.
     *
     * @param userId Identificador del usuario eliminado.
     */
    public void unassignUserFromAssociatedCourse(Long userId) {
        log.info("Llamando al [course-service] para des-asignar al usuario con id [{}] de algún curso", userId);

        this.restClient
                .delete()
                .uri(COURSE_URI.concat("/users/{userId}"), userId)
                .retrieve()
                .toBodilessEntity(); // Operación asincrónica lógica (fire and forget)

        log.info("Fin de la llamada al [course-service] para des-asignar al usuario con id: {}", userId);
    }
}
