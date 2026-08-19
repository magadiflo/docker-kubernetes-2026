import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Factory que genera un guard de rol reutilizable, parametrizado por el rol requerido.
 * A diferencia de authGuard (un guard fijo), roleGuard(role) es una FUNCIÓN QUE RETORNA
 * un CanActivateFn — así podemos usarlo para distintos roles sin duplicar código:
 * canActivate: [roleGuard('ADMIN')], canActivate: [roleGuard('EDITOR')], etc.
 *
 * IMPORTANTE: este guard asume que ya se verificó la autenticación previamente en la
 * cadena de guards (ej. canActivate: [authGuard, roleGuard('ADMIN')]). Por eso NO
 * vuelve a comprobar isAuthenticated() — solo evalúa el rol, evitando duplicar la
 * lógica que authGuard ya resuelve.
 */
export function roleGuard(role: string): CanActivateFn {
  return (route, state) => {
    console.log(`roleGuard - CanActivateFn: ${role}`);

    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.hasRole(role)) {
      return true;
    }

    // Retornar un UrlTree en vez de llamar router.navigate(...) + return false
    // es el patrón recomendado: le indica al Router "redirige aquí" en una sola
    // operación atómica, en vez de disparar una navegación manual por fuera del
    // ciclo de resolución de guards.
    return router.parseUrl('/403');
  };
}
