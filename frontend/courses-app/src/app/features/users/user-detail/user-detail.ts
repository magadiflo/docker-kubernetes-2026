import { Component, effect, inject, input, signal } from '@angular/core';
import { Location } from '@angular/common';
import { finalize } from 'rxjs';

import { UserService } from '../../../core/services/user.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-detail',
  imports: [],
  templateUrl: './user-detail.html',
  styleUrl: './user-detail.scss',
})
export class UserDetail {
  // 🔗 Ver la explicación completa en CourseDetail: debe llamarse "id" (coincidiendo
  // con users.routes.ts, path: ':id') y debe ser "public" para que el Router
  // pueda poblarla mediante withComponentInputBinding().
  public readonly id = input.required<string>();

  private readonly userService = inject(UserService);
  private readonly location = inject(Location);

  protected readonly user = signal<User | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => {
      this.loadUser(Number(this.id()));
    });
  }

  protected goBack(): void {
    this.location.back();
  }

  private loadUser(userId: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.userService
      .findUser(userId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (user) => this.user.set(user),
        error: (error) => {
          console.error('Error al cargar el usuario', error);
          this.errorMessage.set('No se pudo cargar el detalle del usuario.');
        },
      });
  }
}
