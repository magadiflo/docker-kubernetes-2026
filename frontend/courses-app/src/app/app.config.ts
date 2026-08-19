import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { routes } from './app.routes';
import { credentialsInterceptor } from './core/interceptors/credentials-interceptor';
import { AuthService } from './core/services/auth.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),

    // withComponentInputBinding() habilita que los parámetros de ruta (:id) se inyecten
    // automáticamente como "input" en el componente, sin necesidad de leer ActivatedRoute manualmente
    provideRouter(routes, withComponentInputBinding()),

    // Configuración moderna de HttpClient para Angular 22, con nuestro interceptor funcional registrado
    provideHttpClient(withInterceptors([credentialsInterceptor])),

    // Consulta el estado de sesión ANTES de que la aplicación termine de arrancar
    provideAppInitializer(() => {
      const authService = inject(AuthService);
      return firstValueFrom(authService.fetchCurrentUser());
    }),
  ],
};
