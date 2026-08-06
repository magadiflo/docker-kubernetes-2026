import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth-guard';

export default [
  {
    path: '',
    loadComponent: () => import('./layout-page/layout-page').then((m) => m.LayoutPage),
    children: [
      {
        path: '',
        loadComponent: () => import('./courses-list/courses-list').then((m) => m.CoursesList),
      },
      {
        path: ':id',
        loadComponent: () => import('./course-detail/course-detail').then((m) => m.CourseDetail),
        canActivate: [authGuard],
      },
    ],
  },
] satisfies Routes; // Desde desde TypeScript 4.9 se prefiere 'satisfies' sobre 'as' porque verifica el tipo y conserva una inferencia más precisa.
