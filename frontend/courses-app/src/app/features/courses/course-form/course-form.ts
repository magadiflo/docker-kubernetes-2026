import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { CourseService } from '../../../core/services/course.service';
import { CourseRequest } from '../../../core/models/course-request.model';

@Component({
  selector: 'app-course-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './course-form.html',
  styleUrl: './course-form.scss',
})
export class CourseForm {
  // Input OPCIONAL (no .required): la ruta 'new' no trae :id, la ruta ':id/edit' sí.
  // Cuando no viene, este signal simplemente queda undefined.
  public readonly id = input<string>();

  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly courseService = inject(CourseService);

  protected readonly isEditMode = computed(() => !!this.id());
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.group({
    name: this.fb.control('', [Validators.required, Validators.minLength(3)]),
  });

  constructor() {
    effect(() => {
      const courseId = this.id();
      if (courseId) {
        this.loadCourse(Number(courseId));
      }
    });
  }

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const request: CourseRequest = {
      name: this.form.controls.name.value,
    };

    const operation = this.isEditMode()
      ? this.courseService.updateCourse(Number(this.id()), request)
      : this.courseService.saveCourse(request);

    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: (course) => this.router.navigate(['/courses', course.id]),
      error: (error) => {
        console.error(`Error al guardar el curso: ${request.name}`, error);
        this.errorMessage.set('No se pudo guardar el curso.');
      },
    });
  }

  private loadCourse(courseId: number): void {
    this.courseService.findCourse(courseId).subscribe({
      next: (course) => this.form.patchValue({ name: course.name }),
      error: (error) => {
        console.error(`Error cargando el curso: ${courseId}`, error);
        this.errorMessage.set('No se pudo cargar el curso a editar.');
      },
    });
  }
}
