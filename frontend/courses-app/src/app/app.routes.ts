import { Routes } from '@angular/router';
import { authMatchGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: '/courses', pathMatch: 'full' },
  {
    path: 'courses',
    loadChildren: () => import('./features/courses/courses.routes'),
  },
  {
    path: 'users',
    loadChildren: () => import('./features/users/users.routes'),
    canMatch: [authMatchGuard],
  },
  {
    path: '404',
    loadComponent: () => import('./shared/components/not-found/not-found').then((m) => m.NotFound),
  },
  { path: '**', redirectTo: '/404' },
];
