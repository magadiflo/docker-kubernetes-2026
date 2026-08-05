import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/courses', pathMatch: 'full' },
  {
    path: 'courses',
    loadChildren: () => import('./features/courses/courses.routes'),
  },
  {
    path: '404',
    loadComponent: () => import('./shared/components/not-found/not-found').then((m) => m.NotFound),
  },
  { path: '**', redirectTo: '/404' },
];
