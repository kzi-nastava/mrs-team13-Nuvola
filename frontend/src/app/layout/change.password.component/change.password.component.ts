import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {AbstractControl,FormControl,FormGroup,ReactiveFormsModule,ValidationErrors,Validators} from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../auth/services/auth.service';

function passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
  const newPass = group.get('newPassword')?.value;
  const confirm = group.get('confirmNewPassword')?.value;

  if (!newPass || !confirm) return null;
  return newPass === confirm ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-change.password.component',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './change.password.component.html',
  styleUrl: './change.password.component.css',
})
export class ChangePasswordComponent {
hide0 = true;
  hide1 = true;
  hide2 = true;

  done = false;

  form = new FormGroup(
    {
      currentPassword: new FormControl('', Validators.required),
      newPassword: new FormControl('', [Validators.required, Validators.minLength(6)]),
      confirmNewPassword: new FormControl('', Validators.required),
    },
    { validators: passwordsMatchValidator }
  );

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  get currentPassword() {
    return this.form.controls.currentPassword;
  }

  get newPassword() {
    return this.form.controls.newPassword;
  }

  get mismatch(): boolean {
    return !!this.form.errors?.['passwordsMismatch'];
  }

 submit() {
  if (this.form.invalid) {
    this.form.markAllAsTouched();
    return;
  }

  this.authService.changePassword({
    currentPassword: this.form.value.currentPassword!,
    newPassword: this.form.value.newPassword!,
  }).subscribe({
    next: () => {
      this.done = true;

      setTimeout(() => {
        const role = this.authService.getRole();

        if (role === 'ROLE_DRIVER') {
          this.router.navigate(['/driver-account']);
        } else {
          // ADMIN ili REGISTERED_USER
          this.router.navigate(['/account-settings']);
        }
      }, 1000);
    },
    error: () => {
      alert('Current password is incorrect.');
    }
  });
}

}
