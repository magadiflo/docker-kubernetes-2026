import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth-guard';

export default [
  {
    path: '',
    loadComponent: () => import('./layout-page/layout-page').then((m) => m.LayoutPage),
    canActivate: [authGuard],
    children: [
      {
        path: ':id',
        loadComponent: () => import('./user-detail/user-detail').then((m) => m.UserDetail),
      },
    ],
  },
] satisfies Routes;
