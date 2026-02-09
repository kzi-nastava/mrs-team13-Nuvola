import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-activate-email-component',
  imports: [CommonModule, RouterModule],
  template: `
    <div style="max-width: 720px; margin: 0 auto; padding: 24px;">
      <h2>Account activation</h2>

      <p *ngIf="loading">Activating your account...</p>

      <p *ngIf="message" style="color: #22c55e; font-weight: 600;">
        {{ message }}
      </p>

      <p *ngIf="error" style="color: #ef4444; font-weight: 600;">
        {{ error }}
      </p>

      <a *ngIf="message" routerLink="/login">Go to Login</a>
    </div>
  `,

})
export class ActivateEmailComponent implements OnInit {
  loading = true;
  message = '';
  error = '';

  constructor(private route: ActivatedRoute, private authService: AuthService) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.loading = false;
      this.error = 'Missing activation token.';
      return;
    }

    this.authService.activateEmail(token).subscribe({
      next: (res) => {
        this.loading = false;
        this.message = res || 'Account activated successfully. You can now log in.';
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error || 'Activation failed or token expired.';
      },
    });
  }
}
