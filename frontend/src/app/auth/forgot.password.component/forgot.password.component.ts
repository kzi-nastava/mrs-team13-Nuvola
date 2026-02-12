import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-forgot-password-component',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './forgot.password.component.html',
  styleUrl: './forgot.password.component.css',
})
export class ForgotPasswordComponent {
  sent = false;
  loading = false;
  error = '';
  message = '';

  form = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
  });

  constructor(private authService: AuthService) {}

  get email() {
    return this.form.controls.email;
  }

  submit() {
    this.sent = false;
    this.error = '';
    this.message = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    this.authService.forgotPassword(this.email.value!).subscribe({
      next: (res) => {
        this.sent = true;
        this.message = res; // backend vraća text
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error || 'Greška pri slanju linka.';
        this.loading = false;
      }
    });
  }
}
