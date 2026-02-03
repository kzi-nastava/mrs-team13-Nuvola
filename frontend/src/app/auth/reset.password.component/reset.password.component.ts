import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../layout/service/auth.service';
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

constructor(private route: ActivatedRoute, private router: Router, private authService: AuthService) {
  this.token = this.route.snapshot.queryParamMap.get('token');

  if (!this.token) {
    this.token = this.route.snapshot.paramMap.get('token');
  }
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
  if (this.form.invalid || !this.token) {
    this.form.markAllAsTouched();
    return;
  }

  const password = this.form.value.newPassword!;

  this.authService.activateAccount(this.token, password).subscribe({
    next: () => {
      this.done = true;

      setTimeout(() => {
        this.router.navigate(['/login']);
      }, 1000);
    },
    error: () => {
      alert('Activation link is invalid or expired.');
    }
  });
}

}
