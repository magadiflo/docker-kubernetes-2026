package dev.magadiflo.course.app.service;

public interface CourseUserService {
    /**
     * Elimina todas las asociaciones de un usuario en el ecosistema de cursos.
     *
     * @param userId Identificador único del usuario proveniente del user-service.
     */
    void deleteCourseUserByUserId(Long userId);
}
