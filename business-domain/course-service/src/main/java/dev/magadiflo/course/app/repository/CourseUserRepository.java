package dev.magadiflo.course.app.repository;

import dev.magadiflo.course.app.entity.CourseUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseUserRepository extends JpaRepository<CourseUser, Long> {
    /**
     * 🗑️ Elimina la asociación de un usuario con cualquier curso.
     * Útil cuando un usuario es dado de baja en el user-service y
     * debemos limpiar su rastro en el sistema de inscripciones.
     */
    void deleteByUserId(Long userId);
}
