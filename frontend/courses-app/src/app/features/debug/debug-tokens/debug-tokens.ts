import { Component, inject, signal } from '@angular/core';
import { DebugService } from '../../../core/services/debug.service';
import { JsonPipe } from '@angular/common';
import { finalize, forkJoin } from 'rxjs';

@Component({
  selector: 'app-debug-tokens',
  imports: [JsonPipe],
  templateUrl: './debug-tokens.html',
  styleUrl: './debug-tokens.scss',
})
export class DebugTokens {
  private readonly debugService = inject(DebugService);

  protected readonly authorizedInfo = signal<unknown>(null);
  protected readonly principalInfo = signal<unknown>(null);
  protected readonly loading = signal(false);

  protected refresh(): void {
    this.loading.set(true);

    forkJoin({
      authorizedInfo: this.debugService.getAuthorizedInfo(),
      principalInfo: this.debugService.getPrincipalInfo(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ authorizedInfo, principalInfo }) => {
          this.authorizedInfo.set(authorizedInfo);
          this.principalInfo.set(principalInfo);
        },
      });
  }
}
