import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { Navbar } from './shared/components/navbar/navbar';
import { RouteLoadingService } from './core/services/route-loading.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = signal('courses-app');
  protected readonly routerLoading = inject(RouteLoadingService);
}
