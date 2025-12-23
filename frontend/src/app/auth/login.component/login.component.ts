import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NavBarComponent } from '../../layout/nav-bar/nav-bar.component';


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

  get email() {
    return this.form.controls.email;
  }

  get password() {
    return this.form.controls.password;
  }

  togglePassword() {
    this.hidePassword = !this.hidePassword;
  }

  submit() {
    this.submittedOk = false;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    
    this.submittedOk = true;
  }
}

