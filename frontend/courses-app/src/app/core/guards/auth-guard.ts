import { CanActivateFn, CanMatchFn } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * CanMatch
 *
 * Se utiliza principalmente en rutas lazy (loadChildren / loadComponent).
 *
 * Su principal ventaja es que se ejecuta antes de que Angular cargue el
 * componente o módulo correspondiente. Si devuelve false, Angular no considera
 * esa ruta como una coincidencia y, por tanto, evita descargar ese bundle.
 *
 * Es la primera línea de defensa para proteger features completos y reducir
 * descargas innecesarias cuando el usuario no está autenticado.
 */
export const authMatchGuard: CanMatchFn = (route, segments) => {
  console.log('authMatchGuard: CanMatchFn');
  return checkAuthentication();
};

/**
 * CanActivate
 *
 * Protege la activación de una ruta concreta.
 *
 * Se ejecuta cuando Angular ya ha encontrado la ruta que desea activar.
 * Si la ruta era lazy, su código ya fue descargado; este guard únicamente
 * decide si la navegación puede continuar o debe bloquearse.
 *
 * Es ideal para proteger páginas específicas dentro de un feature que ya
 * fue cargado.
 */
export const authGuard: CanActivateFn = (route, state) => {
  console.log('authGuard: CanActivateFn');
  return checkAuthentication();
};

/**
 * Comprueba si existe una sesión autenticada.
 *
 * Si no existe, inicia el flujo OAuth2 redirigiendo al Gateway y
 * devuelve false para cancelar la navegación interna del Angular Router.
 */
const checkAuthentication = (): boolean => {
  const authService = inject(AuthService);
  if (authService.isAuthenticated()) {
    return true;
  }

  authService.login();
  return false;
};
