import { Component, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Location } from '@angular/common';
import { finalize } from 'rxjs';

import { AuthService } from './../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-detail',
  imports: [RouterLink],
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
  protected readonly authService = inject(AuthService);

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

  protected deleteUser(): void {
    if (!confirm('¿Eliminar este usuario? Esta acción no se puede deshacer.')) return;

    this.userService.deleteUser(Number(this.id())).subscribe({
      next: () => this.goBack(),
      error: (error) => {
        console.error(`Error al eliminar el usuario: ${this.id()}`, error);
        this.errorMessage.set(`No se pudo eliminar el usuario con id: ${this.id()}.`);
      },
    });
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
