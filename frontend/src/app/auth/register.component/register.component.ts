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
import { NavBarComponent } from '../../layout/nav-bar/nav-bar.component';

function passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
  const p1 = group.get('password')?.value;
  const p2 = group.get('confirmPassword')?.value;
  if (!p1 || !p2) return null;
  return p1 === p2 ? null : { passwordsMismatch: true };
}


@Component({
  selector: 'app-register-component',
  imports: [CommonModule, ReactiveFormsModule, RouterModule, NavBarComponent],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  hide1 = true;
  hide2 = true;

  
  imagePreviewUrl: string | null = null;


  form = new FormGroup(
  {
    firstName: new FormControl('', [
      Validators.required,
      Validators.maxLength(20),
    ]),
    lastName: new FormControl('', [
      Validators.required,
      Validators.maxLength(25),
    ]),
    email: new FormControl('', [
      Validators.required,
      Validators.email,        
      Validators.maxLength(35) 
    ]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(6), 
    ]),
    confirmPassword: new FormControl('', [Validators.required]),

    phone: new FormControl('', [
      Validators.required,
      Validators.pattern(/^\+381[0-9]+$/), 
    ]),

    address: new FormControl('', [Validators.required]),

    profileImage: new FormControl<File | null>(null), 
  },
  { validators: passwordsMatchValidator }
);

  constructor(private router: Router) {}

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

  registered = false;


  submit() {
    this.registered = false;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

   
    console.log('REGISTER payload:', {
      ...this.form.value,
      profileImage: this.form.value.profileImage ? '(file selected)' : null,
    });

    this.registered = true;
  }
}
