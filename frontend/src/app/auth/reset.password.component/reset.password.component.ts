import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

function passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
  const newPass = group.get('newPassword')?.value;
  const confirm = group.get('confirmNewPassword')?.value;

  if (!newPass || !confirm) return null; 
  return newPass === confirm ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-reset-password-component',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './reset.password.component.html',
  styleUrl: './reset.password.component.css',
})
export class ResetPasswordComponent {
  token: string | null = null;

  hide1 = true;
  hide2 = true;

  done = false;

  form = new FormGroup(
    {
      newPassword: new FormControl('', [Validators.required, Validators.minLength(6)]),
      confirmNewPassword: new FormControl('', [Validators.required]),
    },
    { validators: passwordsMatchValidator }
  );

  constructor(private route: ActivatedRoute, private router: Router) {
    this.token = this.route.snapshot.paramMap.get('token');
  }

  get newPassword() {
    return this.form.controls.newPassword;
  }

  get confirmNewPassword() {
    return this.form.controls.confirmNewPassword;
  }

  get mismatch(): boolean {
    return !!this.form.errors?.['passwordsMismatch'];
  }

  submit() {
    this.done = false;

   
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

   
    console.log('Reset password request with token:', this.token);
    this.done = true;

    setTimeout(() => this.router.navigate(['/login']), 700);
  }
}
