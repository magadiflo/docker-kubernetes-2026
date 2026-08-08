import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Course } from '../models/course.model';
import { CourseRequest } from '../models/course-request.model';
import { User } from '../models/user.model';

@Service()
export class CourseService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.gatewayServerUrl}/api/v1/courses`;

  /**
   * Obtiene el listado de cursos.
   * @param loadRelations Si es true, cada curso incluye la lista de usuarios asociados
   *                       (dispara, en el backend, la llamada interna course-service -> user-service).
   */
  findAllCourses(loadRelations: boolean = false): Observable<Course[]> {
    const params = new HttpParams().set('loadRelations', loadRelations);
    return this.http.get<Course[]>(this.baseUrl, { params });
  }

  /**
   * Obtiene el detalle de un curso específico.
   * Endpoint protegido en el backend: requiere ROLE_USER como mínimo.
   */
  findCourse(courseId: number, loadRelations: boolean = false): Observable<Course> {
    const params = new HttpParams().set('loadRelations', loadRelations);
    return this.http.get<Course>(`${this.baseUrl}/${courseId}`, { params });
  }

  /** Requiere ROLE_ADMIN. */
  saveCourse(request: CourseRequest): Observable<Course> {
    return this.http.post<Course>(this.baseUrl, request);
  }

  /** Requiere ROLE_ADMIN. */
  updateCourse(courseId: number, request: CourseRequest): Observable<Course> {
    return this.http.put<Course>(`${this.baseUrl}/${courseId}`, request);
  }

  /** Requiere ROLE_ADMIN. */
  deleteCourse(courseId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${courseId}`);
  }

  /** Requiere ROLE_ADMIN. Asigna un usuario ya existente a un curso. */
  assignExistingUserToCourse(courseId: number, userId: number): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/${courseId}/users/${userId}`, {});
  }

  /** Requiere ROLE_ADMIN. */
  unassignUserFromCourse(courseId: number, userId: number): Observable<User> {
    return this.http.delete<User>(`${this.baseUrl}/${courseId}/users/${userId}`);
  }
}
