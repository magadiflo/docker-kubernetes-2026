import { inject, Service, signal } from '@angular/core';
import {
  NavigationCancel,
  NavigationEnd,
  NavigationError,
  NavigationStart,
  Router,
} from '@angular/router';
import { filter } from 'rxjs';

@Service()
export class RouteLoadingService {
  private readonly router = inject(Router);
  private readonly loading = signal(false);
  public readonly isLoading = this.loading.asReadonly();

  constructor() {
    this.router.events
      .pipe(
        filter(
          (event) =>
            event instanceof NavigationStart ||
            event instanceof NavigationEnd ||
            event instanceof NavigationCancel ||
            event instanceof NavigationError,
        ),
      )
      .subscribe((event) => {
        this.loading.set(event instanceof NavigationStart);
      });
  }
}
