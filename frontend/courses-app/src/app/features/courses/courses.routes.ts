import { Routes } from '@angular/router';

export default [
  {
    path: '',
    loadComponent: () => import('./layout-page/layout-page').then((m) => m.LayoutPage),
    children: [
      {
        path: '',
        loadComponent: () => import('./courses-list/courses-list').then((m) => m.CoursesList),
      },
    ],
  },
] satisfies Routes; // Desde desde TypeScript 4.9 se prefiere 'satisfies' sobre 'as' porque verifica el tipo y conserva una inferencia más precisa.
