import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Course } from '../models/course.model';

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
}
