import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { User } from '../models/user.model';
import { UserRequest } from '../models/user-request.model';

@Service()
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.gatewayServerUrl}/api/v1/users`;

  /**
   * Obtiene el detalle de un usuario específico.
   * Endpoint protegido en el backend: requiere ROLE_USER como mínimo.
   */
  findUser(userId: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${userId}`);
  }

  /** hasAnyRole('USER', 'ADMIN') en el backend. Se usa para el selector de "asignar usuario existente". */
  findAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.baseUrl);
  }

  /** Requiere ROLE_ADMIN. */
  saveUser(request: UserRequest): Observable<User> {
    return this.http.post<User>(this.baseUrl, request);
  }

  /** Requiere ROLE_ADMIN. */
  updateUser(userId: number, request: UserRequest): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/${userId}`, request);
  }

  /** Requiere ROLE_ADMIN. */
  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${userId}`);
  }
}
