import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-forgot-password-component',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './forgot.password.component.html',
  styleUrl: './forgot.password.component.css',
})
export class ForgotPasswordComponent {
  sent = false;

  form = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
  });

  constructor(private router: Router) {}

  get email() {
    return this.form.controls.email;
  }

  submit() {
    this.sent = false;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    
    this.sent = true;

    
    setTimeout(() => {
      this.router.navigate(['/reset-password']);
    }, 600);
  }
}
