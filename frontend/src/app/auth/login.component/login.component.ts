import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { NavBarComponent } from '../../layout/nav-bar/nav-bar.component';
import { AuthService } from '../../layout/service/auth.service';


@Component({
  selector: 'app-login-component',
  imports: [CommonModule, ReactiveFormsModule, RouterModule, NavBarComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  hidePassword = true;
  submittedOk = false;

  form = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required]),
    rememberMe: new FormControl(false),
  });

  constructor(private authService: AuthService,
    private router: Router) {}

  get email() {
    return this.form.controls.email;
  }

  get password() {
    return this.form.controls.password;
  }

  togglePassword() {
    this.hidePassword = !this.hidePassword;
  }

  onForgotPassword() {
    const email = this.email.value;
    const username = email?.split('@')[0] || 'user';
    this.router.navigate(['/forgot-password', username]);
  }

  submit() {
    this.submittedOk = false;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const email = this.email.value;
    
    const username = email?.split('@')[0] || 'user';
    
    this.authService.login(username);
    this.submittedOk = true;
    this.router.navigate(['/ride-history/', username]);

  }
}

