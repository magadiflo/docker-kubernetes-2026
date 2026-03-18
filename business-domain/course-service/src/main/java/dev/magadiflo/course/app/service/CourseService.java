package dev.magadiflo.course.app.service;

import dev.magadiflo.course.app.dto.CourseRequest;
import dev.magadiflo.course.app.dto.CourseResponse;
import dev.magadiflo.course.app.dto.UserRequest;
import dev.magadiflo.course.app.dto.UserResponse;

import java.util.List;

public interface CourseService {
    /**
     * Recupera el listado de cursos.
     *
     * @param loadRelations Si es true, hidrata los datos de los usuarios desde user-service.
     */
    List<CourseResponse> findAllCourses(boolean loadRelations);

    /**
     * Busca un curso específico por su ID.
     *
     * @param loadRelations Si es true, incluye el detalle completo de los alumnos inscritos.
     */
    CourseResponse findCourse(Long courseId, boolean loadRelations);

    CourseResponse saveCourse(CourseRequest courseRequest);

    CourseResponse updateCourse(Long courseId, CourseRequest courseRequest);

    void deleteCourse(Long courseId);

    //-- 🌐 Canales de comunicación con el microservicio user-service ---
    UserResponse assignExistingUserToCourse(Long userId, Long courseId);

    UserResponse createUserAndAssignItToCourse(UserRequest userRequest, Long courseId);

    UserResponse unassignUserFromACourse(Long userId, Long courseId);
}
