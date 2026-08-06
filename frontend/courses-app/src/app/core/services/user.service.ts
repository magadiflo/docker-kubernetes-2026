import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { User } from '../models/user.model';

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
}
