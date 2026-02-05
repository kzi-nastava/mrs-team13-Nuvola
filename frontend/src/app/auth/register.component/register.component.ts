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
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { RegisterModel } from '../model/register.model';

function passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
  const p1 = group.get('password')?.value;
  const p2 = group.get('confirmPassword')?.value;
  if (!p1 || !p2) return null;
  return p1 === p2 ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-register-component',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  hide1 = true;
  hide2 = true;

  imagePreviewUrl: string | null = null;

  registered = false;
  isSubmitting = false;

  // Poruke za UI (možeš da prikažeš u template-u)
  apiError: string | null = null;

  form = new FormGroup(
    {
      firstName: new FormControl('', [Validators.required, Validators.maxLength(20)]),
      lastName: new FormControl('', [Validators.required, Validators.maxLength(25)]),

      email: new FormControl('', [
        Validators.required,
        Validators.email,
        Validators.maxLength(35),
      ]),

      password: new FormControl('', [Validators.required, Validators.minLength(6)]),
      confirmPassword: new FormControl('', [Validators.required]),

      phone: new FormControl('', [
        Validators.required,
        Validators.pattern(/^\+381[0-9]+$/),
      ]),

      address: new FormControl('', [Validators.required]),

      // ostavljamo samo preview; NE šaljemo na backend odmah (jer endpoint je često auth-protected)
      profileImage: new FormControl<File | null>(null),
    },
    { validators: passwordsMatchValidator }
  );

  constructor(private router: Router, private authService: AuthService) {}

  get firstName() { return this.form.controls.firstName; }
  get lastName() { return this.form.controls.lastName; }
  get email() { return this.form.controls.email; }
  get password() { return this.form.controls.password; }
  get confirmPassword() { return this.form.controls.confirmPassword; }
  get phone() { return this.form.controls.phone; }
  get address() { return this.form.controls.address; }

  get mismatch(): boolean {
    return !!this.form.errors?.['passwordsMismatch'];
  }

  onFileSelected(e: Event) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.form.controls.profileImage.setValue(file);

    if (file) {
      const reader = new FileReader();
      reader.onload = () => (this.imagePreviewUrl = String(reader.result));
      reader.readAsDataURL(file);
    } else {
      this.imagePreviewUrl = null;
    }
  }

  submit() {
    this.registered = false;
    this.apiError = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;

    const email = (this.email.value ?? '').trim().toLowerCase();
    const password = this.password.value ?? '';
    const confirmPassword = this.confirmPassword.value ?? '';

    
    const username = email;

    const registerData: RegisterModel = {
      email,
      username,
      password,
      confirmPassword,
      firstName: this.firstName.value ?? '',
      lastName: this.lastName.value ?? '',
      address: this.address.value ?? '',
      phone: this.phone.value ?? '',
      picture: '', 
    };

    console.log('REGISTER DATA FINAL:', registerData);

    this.authService.register(registerData).subscribe({
      next: (_) => {
        this.isSubmitting = false;
        this.registered = true;

        
        alert('Registracija uspešna! Proveri email i aktiviraj nalog pre prijave.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isSubmitting = false;
        console.error('Registration failed:', err);

        // Najčešći slučaj kod tebe: "Username already exists"
        // Backend ti trenutno vraća 500, ali poruka u logu je conflict.
        const backendMessage =
          err?.error?.message ||
          err?.error?.error ||
          (typeof err?.error === 'string' ? err.error : null) ||
          err?.message;

        const msg = String(backendMessage ?? '');

        if (
          err?.status === 409 ||
          msg.toLowerCase().includes('already exists') ||
          msg.toLowerCase().includes('conflict') ||
          msg.toLowerCase().includes('username')
        ) {
          this.apiError = 'Nalog sa ovim email/username već postoji. Probaj drugi email ili se uloguj.';
          alert(this.apiError);
          return;
        }

        this.apiError = 'Registracija nije uspela. Proveri podatke i pokušaj ponovo.';
        alert(this.apiError);
      },
    });
  }
}
