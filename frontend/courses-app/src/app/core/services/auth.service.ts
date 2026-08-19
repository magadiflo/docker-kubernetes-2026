import { computed, inject, Service, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserSession } from '../models/user-session.model';

const UNAUTHENTICATED_SESSION: UserSession = { authenticated: false };

@Service()
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl: string = environment.gatewayServerUrl;

  // 🔒 Estado privado: nadie fuera de este servicio puede modificarlo directamente
  private readonly userSession = signal<UserSession>(UNAUTHENTICATED_SESSION);

  // 📖 Exposición pública, de solo lectura, mediante computed signals
  readonly isAuthenticated = computed(() => this.userSession().authenticated);
  readonly username = computed(() => this.userSession().username);
  readonly roles = computed(() => this.userSession().roles ?? []);

  /**
   * Consulta el estado de sesión actual contra el gateway-server y actualiza el signal interno.
   * Se usa tanto al arrancar la aplicación como después de un login/logout.
   *
   * Importante: este método NUNCA propaga un error hacia quien lo suscribe. Si el Gateway no
   * responde (caído, red interrumpida, etc.), se asume un estado "no autenticado" por defecto,
   * en vez de dejar que el error se propague. Esto es crítico porque este método se usa dentro
   * de provideAppInitializer, y un Observable que emite error ahí bloquea el arranque completo
   * de la aplicación (pantalla en blanco).
   */
  fetchCurrentUser(): Observable<UserSession> {
    return this.http.get<UserSession>(`${this.baseUrl}/api/users/me`).pipe(
      tap((userSession: UserSession) => this.userSession.set(userSession)),
      catchError((error) => {
        console.error('Error al recuperar la sesión del usuario actual:', error);
        this.userSession.set(UNAUTHENTICATED_SESSION);
        return of(UNAUTHENTICATED_SESSION);
      }),
    );
  }

  /**
   * Inicia el flujo de login. Navegación REAL del navegador (no HttpClient),
   * ya que el flujo OAuth2 involucra múltiples redirects que HttpClient no puede seguir
   * correctamente al tratarse de una petición AJAX.
   */
  login(): void {
    window.location.href = `${this.baseUrl}/auth/login`;
  }

  /**
   * Cierra la sesión. Igual que el login, se dispara mediante un formulario HTML real
   * con método POST, no mediante HttpClient — por la misma razón: son varios redirects
   * encadenados (Gateway -> Authorization Server -> Gateway -> Angular) que solo el
   * navegador puede seguir correctamente como navegación de página completa.
   */
  logout(): void {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `${this.baseUrl}/logout`;
    document.body.appendChild(form);
    form.submit();
  }

  /**
   * Verifica si el usuario actual posee un rol específico.
   * @param role Nombre del rol SIN el prefijo "ROLE_" (ej: "ADMIN", "USER")
   */
  hasRole(role: string): boolean {
    return this.roles().includes(`ROLE_${role}`);
  }
}
