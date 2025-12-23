import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.css']
})
export class AccountComponent {

  accountForm: FormGroup;
  successMessage = '';
  errorMessage = '';

  constructor(private fb: FormBuilder) {
    this.accountForm = this.fb.group({
      firstName: [
        'Milica',
        [Validators.required, Validators.pattern(/^[A-Za-zČĆŠĐŽčćšđž]+$/)]
      ],
      lastName: [
        'Lukic',
        [Validators.required, Validators.pattern(/^[A-Za-zČĆŠĐŽčćšđž]+$/)]
      ],
      email: [
        'milicalukic@gmail.com',
        [Validators.required, Validators.email]
      ],
      phone: [
        '+381 64 556655',
        [Validators.required, Validators.pattern(/^[0-9+\s]+$/)]
      ],
      address: [
        'Fruskogorska 37',
        Validators.required
      ]
    });
  }

  onSave(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.accountForm.invalid) {
      this.errorMessage = 'Please fix the errors before saving.';
      this.accountForm.markAllAsTouched();
      return;
    }

    this.successMessage = 'Changes saved!';
    setTimeout(() => {
      this.successMessage = '';
    }, 3000);
  }

  onChangePassword(): void {
    console.log('Change password clicked');
  }
}
