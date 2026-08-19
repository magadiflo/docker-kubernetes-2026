import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

@Service()
export class DebugService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.gatewayServerUrl}/test/oauth2-client`;

  /** Muestra el access_token/refresh_token vigentes — útil para observar el refresh automático. */
  getAuthorizedInfo(): Observable<unknown> {
    return this.http.get(`${this.baseUrl}/authorized-info`);
  }

  /** Muestra el id_token y las authorities reales usadas por Spring Security. */
  getPrincipalInfo(): Observable<unknown> {
    return this.http.get(`${this.baseUrl}/principal-info`);
  }
}
