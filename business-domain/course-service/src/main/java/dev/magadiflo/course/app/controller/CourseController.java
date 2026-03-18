package dev.magadiflo.course.app.controller;

import dev.magadiflo.course.app.dto.CourseRequest;
import dev.magadiflo.course.app.dto.CourseResponse;
import dev.magadiflo.course.app.dto.UserRequest;
import dev.magadiflo.course.app.dto.UserResponse;
import dev.magadiflo.course.app.service.CourseService;
import dev.magadiflo.course.app.service.CourseUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/{version}/courses", version = "1")
public class CourseController {

    private final CourseService courseService;
    private final CourseUserService courseUserService;

    @GetMapping
    public ResponseEntity<List<CourseResponse>> findAllCourses(@RequestParam(required = false, defaultValue = "false") boolean loadRelations) {
        return ResponseEntity.ok(this.courseService.findAllCourses(loadRelations));
    }

    @GetMapping(path = "/{courseId}")
    public ResponseEntity<CourseResponse> findCourse(@PathVariable Long courseId,
                                                     @RequestParam(required = false, defaultValue = "false") boolean loadRelations) {
        return ResponseEntity.ok(this.courseService.findCourse(courseId, loadRelations));
    }

    @PostMapping
    public ResponseEntity<CourseResponse> saveCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse courseResponse = this.courseService.saveCourse(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{courseId}")
                .buildAndExpand(courseResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(courseResponse);
    }

    @PutMapping(path = "/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long courseId, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(this.courseService.updateCourse(courseId, request));
    }

    @DeleteMapping(path = "/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        this.courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    // --- 🛰️ Comunicación: [course-service] → [user-service] ---

    /**
     * 🔗 Asigna un usuario existente en el sistema maestro a un curso local.
     */
    @PostMapping(path = "/{courseId}/users/{userId}")
    public ResponseEntity<UserResponse> assignExistingUserToCourse(@PathVariable Long courseId,
                                                                   @PathVariable Long userId) {
        return ResponseEntity.ok(this.courseService.assignExistingUserToCourse(userId, courseId));
    }

    /**
     * 🆕 Registra un nuevo usuario en el sistema maestro y lo inscribe automáticamente en un curso.
     */
    @PostMapping(path = "/{courseId}/users")
    public ResponseEntity<UserResponse> createUserAndAssignItToCourse(@Valid @RequestBody UserRequest userRequest,
                                                                      @PathVariable Long courseId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.courseService.createUserAndAssignItToCourse(userRequest, courseId));
    }

    /**
     * ✂️ Desvincula a un usuario de un curso específico.
     */
    @DeleteMapping(path = "/{courseId}/users/{userId}")
    public ResponseEntity<UserResponse> unassignUserFromACourse(@PathVariable Long courseId, @PathVariable Long userId) {
        return ResponseEntity.ok(this.courseService.unassignUserFromACourse(userId, courseId));
    }

    // --- 🧹 Comunicación: [course-service] ← [user-service] ---

    /**
     * 🗑️ Endpoint de limpieza: Elimina la presencia de un usuario en tod0 el sistema de cursos.
     * Invocado por el user-service antes de una eliminación definitiva.
     */
    @DeleteMapping(path = "/users/{userId}")
    public ResponseEntity<Void> unassignUserFromAssociatedCourse(@PathVariable Long userId) {
        this.courseUserService.deleteCourseUserByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
