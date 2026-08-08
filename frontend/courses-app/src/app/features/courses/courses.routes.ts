import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth-guard';
import { roleGuard } from '../../core/guards/role-guard';

export default [
  {
    path: '',
    loadComponent: () => import('./layout-page/layout-page').then((m) => m.LayoutPage),
    children: [
      {
        path: '',
        loadComponent: () => import('./courses-list/courses-list').then((m) => m.CoursesList),
      },
      // ⚠️ "new" debe declararse ANTES de ":id" — ambos son un único segmento, y Angular
      // evalúa las rutas en orden. Si ":id" fuera primero, "new" se interpretaría como
      // un valor de :id en vez de coincidir con esta ruta literal.
      {
        path: 'new',
        loadComponent: () => import('./course-form/course-form').then((m) => m.CourseForm),
        canActivate: [authGuard, roleGuard('ADMIN')],
      },
      {
        path: ':id',
        loadComponent: () => import('./course-detail/course-detail').then((m) => m.CourseDetail),
        canActivate: [authGuard, roleGuard('USER')],
      },
      {
        path: ':id/edit',
        loadComponent: () => import('./course-form/course-form').then((m) => m.CourseForm),
        canActivate: [authGuard, roleGuard('ADMIN')],
      },
    ],
  },
] satisfies Routes; // Desde desde TypeScript 4.9 se prefiere 'satisfies' sobre 'as' porque verifica el tipo y conserva una inferencia más precisa.
