import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Course } from '../../../core/models/course.model';
import { CourseService } from '../../../core/services/course.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-courses-list',
  imports: [RouterLink],
  templateUrl: './courses-list.html',
  styleUrl: './courses-list.scss',
})
export class CoursesList implements OnInit {
  private readonly courseService = inject(CourseService);
  protected readonly authService = inject(AuthService);

  // 📦 Estado local del componente, expresado con signals
  protected readonly courses = signal<Course[]>([]);
  protected readonly loading = signal<boolean>(true);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.courseService
      .findAllCourses(true)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (courses: Course[]) => this.courses.set(courses),
        error: (error: unknown) => this.errorMessage.set('No se pudo cargar el listado de cursos'),
      });
  }
}
