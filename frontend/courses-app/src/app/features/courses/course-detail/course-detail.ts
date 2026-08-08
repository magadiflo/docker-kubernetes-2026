import { Component, effect, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { CourseService } from '../../../core/services/course.service';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { Course } from '../../../core/models/course.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-course-detail',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './course-detail.html',
  styleUrl: './course-detail.scss',
})
export class CourseDetail {
  // 🔗 Angular puebla automáticamente esta propiedad con el valor del segmento dinámico
  // de la ruta (gracias a withComponentInputBinding() configurado en app.config.ts).
  // El nombre de esta propiedad ("id") debe coincidir EXACTAMENTE con el nombre del
  // parámetro definido en courses.routes.ts (path: ':id'). Si aquí la llamáramos
  // "courseId" en vez de "id", Angular no la poblaría, porque busca la propiedad por
  // coincidencia de nombre con el segmento de la ruta, no por posición ni por tipo.
  //
  // Debe ser "public" (no "protected" ni "private"): el binding lo asigna el Router
  // desde fuera de esta clase, y Angular actualmente no soporta inputs no públicos
  // para este mecanismo — usar protected/private rompe silenciosamente el binding.
  public readonly id = input.required<string>();

  private readonly courseService = inject(CourseService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  protected readonly authService = inject(AuthService);

  protected readonly course = signal<Course | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  // Solo se pueblan si el usuario es ADMIN (ver efecto abajo)
  protected readonly assignableUsers = signal<User[]>([]);
  protected readonly form = this.fb.group({
    selectedUserId: this.fb.control<number | null>(null, [Validators.required]),
  });

  private get selectedUserIdValue(): number | null {
    return this.form.controls.selectedUserId.value;
  }

  constructor() {
    // Se ejecuta tanto en la carga inicial como en cada cambio posterior del signal "id"
    // (por ejemplo, al navegar de /courses/1 a /courses/2 sin recargar la página completa,
    // algo que ngOnInit() NO detectaría, ya que Angular reutiliza la misma instancia del
    // componente cuando solo cambia el parámetro de una ruta ya activa).
    effect(() => {
      this.loadCourse(Number(this.id()));
    });

    effect(() => {
      if (this.authService.hasRole('ADMIN')) {
        this.userService.findAllUsers().subscribe((users) => this.assignableUsers.set(users));
      }
    });
  }

  protected deleteCourse(): void {
    if (!confirm('¿Eliminar este curso? Esta acción no se puede deshacer.')) return;

    this.courseService.deleteCourse(Number(this.id())).subscribe({
      next: () => this.router.navigate(['/courses']),
      error: () => this.errorMessage.set('No se pudo eliminar el curso.'),
    });
  }

  protected assignUser(): void {
    if (!this.selectedUserIdValue) return;

    this.courseService
      .assignExistingUserToCourse(Number(this.id()), this.selectedUserIdValue)
      .subscribe({
        next: () => this.loadCourse(Number(this.id())),
        error: () => this.errorMessage.set('No se pudo asignar el usuario.'),
      });
  }

  protected unassignUser(userId: number): void {
    this.courseService.unassignUserFromCourse(Number(this.id()), userId).subscribe({
      next: () => this.loadCourse(Number(this.id())),
      error: () => this.errorMessage.set('No se pudo desvincular el usuario.'),
    });
  }

  private loadCourse(courseId: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.courseService
      .findCourse(courseId, true)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (course) => this.course.set(course),
        error: (error) => {
          console.error('Error al cargar el curso', error);
          this.errorMessage.set('No se pudo cargar el curso. Intenta nuevamente más tarde.');
        },
      });
  }
}
