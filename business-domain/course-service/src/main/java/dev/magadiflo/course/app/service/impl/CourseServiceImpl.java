package dev.magadiflo.course.app.service.impl;

import dev.magadiflo.course.app.client.UserServiceClient;
import dev.magadiflo.course.app.dto.CourseRequest;
import dev.magadiflo.course.app.dto.CourseResponse;
import dev.magadiflo.course.app.dto.UserRequest;
import dev.magadiflo.course.app.dto.UserResponse;
import dev.magadiflo.course.app.entity.Course;
import dev.magadiflo.course.app.entity.CourseUser;
import dev.magadiflo.course.app.exception.CourseNotFoundException;
import dev.magadiflo.course.app.mapper.CourseMapper;
import dev.magadiflo.course.app.mapper.CourseUserMapper;
import dev.magadiflo.course.app.repository.CourseRepository;
import dev.magadiflo.course.app.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final UserServiceClient userServiceClient;
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CourseUserMapper courseUserMapper;

    // --- 🔍 Operaciones de Consulta ---
    @Override
    public List<CourseResponse> findAllCourses() {
        return this.courseRepository.findAll()
                .stream()
                .map(this.courseMapper::toCourseResponse)
                .toList();
    }

    @Override
    public CourseResponse findCourse(Long courseId) {
        Course course = this.findCourseOrThrow(courseId);
        return this.courseMapper.toCourseResponse(course);
    }

    // --- 💾 Operaciones de Persistencia Local ---
    @Override
    @Transactional
    public CourseResponse saveCourse(CourseRequest courseRequest) {
        Course savedCourse = this.courseRepository.save(this.courseMapper.toCourse(courseRequest));
        return this.courseMapper.toCourseResponse(savedCourse);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long courseId, CourseRequest courseRequest) {
        Course course = this.findCourseOrThrow(courseId);
        Course updateCourse = this.courseMapper.toUpdateCourse(course, courseRequest);
        this.courseRepository.save(updateCourse);
        return this.courseMapper.toCourseResponse(updateCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = this.findCourseOrThrow(courseId);
        this.courseRepository.delete(course);
    }

    // --- 📡 Integración con Microservicio Remoto (user-service) ---
    @Override
    @Transactional
    public UserResponse assignExistingUserToCourse(Long userId, Long courseId) {
        Course course = this.findCourseOrThrow(courseId);
        // Consulta remota al sistema maestro de usuarios
        UserResponse userResponse = this.userServiceClient.getUserFromUserService(userId);
        return this.assignUserToCourse(userResponse, course);
    }

    @Override
    @Transactional
    public UserResponse createUserAndAssignItToCourse(UserRequest userRequest, Long courseId) {
        Course course = this.findCourseOrThrow(courseId);
        // Registro remoto y obtención de respuesta con ID generado
        UserResponse userResponse = this.userServiceClient.createUserInUserService(userRequest);
        return this.assignUserToCourse(userResponse, course);
    }

    @Override
    @Transactional
    public UserResponse unassignUserFromACourse(Long userId, Long courseId) {
        Course course = this.findCourseOrThrow(courseId);
        UserResponse userResponse = this.userServiceClient.getUserFromUserService(userId);

        // Transformación de DTO a Entidad para gestión de colección
        CourseUser courseUser = this.courseUserMapper.toCourseUser(userResponse);

        log.info("Eliminando CourseUser con userId [{}] del curso [{}]", courseUser.getUserId(), course.getName());

        /* * 💡 Nota Técnica: La eliminación se basa en el method equals() de CourseUser,
         * comparando exclusivamente el userId para identificar al objeto en la colección.
         */
        course.getCourseUsers().remove(courseUser);

        this.courseRepository.save(course);
        return userResponse;
    }

    // --- 🛠️ Métodos Privados de Soporte (Helper Methods) ---
    private Course findCourseOrThrow(Long courseId) {
        return this.courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    private UserResponse assignUserToCourse(UserResponse userResponse, Course course) {
        CourseUser courseUser = this.courseUserMapper.toCourseUser(userResponse);

        log.info("Agregando CourseUser con userId [{}] al curso [{}]", courseUser.getUserId(), course.getName());
        course.getCourseUsers().add(courseUser);

        this.courseRepository.save(course);
        return userResponse;
    }

    /**
     * Extrae los identificadores únicos de usuario asociados a un curso específico.
     * <p>
     * Este método mapea la colección de entidades de relación local (CourseUser)
     * a una colección de IDs nativos, facilitando la consulta masiva hacia el user-service.
     *
     * @param course Entidad del curso que contiene la lista de usuarios asociados.
     * @return Collection<Long> Conjunto de IDs de usuario listos para la hidratación remota.
     */
    private Collection<Long> extractUserIdsFromCourse(Course course) {
        return course.getCourseUsers()
                .stream()
                .map(CourseUser::getUserId)
                .toList();
    }
}
