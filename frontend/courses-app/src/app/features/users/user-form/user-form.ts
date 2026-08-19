import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { UserService } from '../../../core/services/user.service';
import { UserRequest } from '../../../core/models/user-request.model';

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule],
  templateUrl: './user-form.html',
  styleUrl: './user-form.scss',
})
export class UserForm {
  public readonly id = input<string>();

  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly userService = inject(UserService);

  protected readonly isEditMode = computed(() => !!this.id());
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.group({
    name: this.fb.control('', [Validators.required, Validators.minLength(3)]),
    email: this.fb.control('', [Validators.required, Validators.email]),
    password: this.fb.control('', [Validators.required, Validators.minLength(6)]),
  });

  constructor() {
    effect(() => {
      const userId = this.id();
      if (userId) {
        this.loadUser(Number(userId));
      }
    });
  }

  protected goBack(): void {
    this.location.back();
  }

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const request: UserRequest = this.form.getRawValue();

    const operation = this.isEditMode()
      ? this.userService.updateUser(Number(this.id()), request)
      : this.userService.saveUser(request);

    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: (user) => this.router.navigate(['/users', user.id]),
      error: (error) => {
        console.error(`Error al guardar el usuario: ${request.name}`, error);
        this.errorMessage.set('No se pudo guardar el usuario.');
      },
    });
  }

  private loadUser(userId: number): void {
    this.userService.findUser(userId).subscribe({
      next: (user) => this.form.patchValue({ name: user.name, email: user.email }),
      error: (error) => {
        console.error(`Error cargando el usuario: ${userId}`, error);
        this.errorMessage.set('No se pudo cargar el usuario a editar.');
      },
    });
  }
}
