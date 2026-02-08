import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

function passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
  const newPass = group.get('newPassword')?.value;
  const confirm = group.get('confirmNewPassword')?.value;
  if (!newPass || !confirm) return null;
  return newPass === confirm ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-reset-password-component',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './reset.password.component.html',
  styleUrl: './reset.password.component.css',
})
export class ResetPasswordComponent {
  hideNewPassword = true;
  hideConfirmPassword = true;

  loading = false;
  sent = false;
  error = '';
  message = '';

  token = '';

  form = new FormGroup(
    {
      newPassword: new FormControl('', [Validators.required, Validators.minLength(8)]),
      confirmNewPassword: new FormControl('', [Validators.required]),
    },
    { validators: passwordMatchValidator }
  );

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.error = 'Token nedostaje u linku.';
    }
  }

  get newPassword() {
    return this.form.controls.newPassword;
  }

  get confirmNewPassword() {
    return this.form.controls.confirmNewPassword;
  }

  toggleNewPassword() {
    this.hideNewPassword = !this.hideNewPassword;
  }

  toggleConfirmPassword() {
    this.hideConfirmPassword = !this.hideConfirmPassword;
  }

  submit() {
    this.sent = false;
    this.error = '';
    this.message = '';

    if (!this.token) {
      this.error = 'Token nedostaje u linku.';
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    // backend: POST /api/auth/reset-password?token=...  body: { newPassword, confirmNewPassword }
    this.authService
      .resetPassword(this.token, this.newPassword.value!, this.confirmNewPassword.value!)
      .subscribe({
        next: (res) => {
          this.sent = true;
          this.message = res; // backend vraća text
          this.loading = false;

          // opcionalno: prebaci na login posle 1.5s
          setTimeout(() => this.router.navigate(['/login']), 1500);
        },
        error: (err) => {
          this.error = err?.error || 'Greška pri resetovanju lozinke.';
          this.loading = false;
        },
      });
  }
}
