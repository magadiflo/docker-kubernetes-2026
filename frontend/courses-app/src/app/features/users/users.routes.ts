import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth-guard';
import { roleGuard } from '../../core/guards/role-guard';

export default [
  {
    path: '',
    loadComponent: () => import('./layout-page/layout-page').then((m) => m.LayoutPage),
    canActivate: [authGuard],
    children: [
      {
        path: 'new',
        loadComponent: () => import('./user-form/user-form').then((m) => m.UserForm),
        canActivate: [roleGuard('ADMIN')],
      },
      {
        path: ':id',
        loadComponent: () => import('./user-detail/user-detail').then((m) => m.UserDetail),
        canActivate: [roleGuard('USER')],
      },
      {
        path: ':id/edit',
        loadComponent: () => import('./user-form/user-form').then((m) => m.UserForm),
        canActivate: [roleGuard('ADMIN')],
      },
    ],
  },
] satisfies Routes;
