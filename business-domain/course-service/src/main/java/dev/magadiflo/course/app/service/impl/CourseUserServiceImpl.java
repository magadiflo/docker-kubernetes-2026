package dev.magadiflo.course.app.service.impl;

import dev.magadiflo.course.app.repository.CourseUserRepository;
import dev.magadiflo.course.app.service.CourseUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CourseUserServiceImpl implements CourseUserService {

    private final CourseUserRepository courseUserRepository;

    /**
     * Ejecuta la desvinculación masiva de un usuario.
     * <p>
     * Se utiliza un enfoque directo al repositorio para eliminar cualquier
     * coincidencia del userId en la tabla asociativa (course_users)
     */
    @Override
    @Transactional
    public void deleteCourseUserByUserId(Long userId) {
        /* 💡 Nota de Diseño: No se realiza una validación previa de existencia (findById).
         * Si el usuario no está asociado a ningún curso, la operación simplemente
         * completa con éxito sin afectar el estado del sistema, optimizando el tiempo de respuesta.
         */
        this.courseUserRepository.deleteByUserId(userId);
    }
}
