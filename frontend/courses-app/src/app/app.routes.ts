import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/courses/courses-list/courses-list').then((m) => m.CoursesList),
  },
];
