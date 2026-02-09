  import { Component, ChangeDetectorRef } from '@angular/core';
  import { CommonModule } from '@angular/common';
  import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
  import { AccountService } from '../../services/account.service';
  import { Router, RouterModule } from '@angular/router';

  @Component({
    selector: 'app-account',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './account.component.html',
    styleUrls: ['./account.component.css']
  })
  export class AccountComponent {

    profilePreview: string | null = null;
    accountForm: FormGroup;
    successMessage = '';
    errorMessage = '';

    constructor(private fb: FormBuilder, private cdr: ChangeDetectorRef, private accountService: AccountService, private router: Router) {
    this.accountForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.pattern(/^[A-Za-zČĆŠĐŽčćšđž]+$/)]],
      lastName: ['', [Validators.required, Validators.pattern(/^[A-Za-zČĆŠĐŽčćšđž]+$/)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9+\s]+$/)]],
      address: ['', Validators.required]
    });
  }
ngOnInit() {
  this.accountService.getProfile().subscribe(profile => {
    this.accountForm.patchValue(profile);
    this.profilePreview = this.getImageUrl(profile.picture);
    this.accountForm.get('email')?.disable();
  });
}

onSave(): void {
  if (this.accountForm.invalid) {
    this.accountForm.markAllAsTouched();
    return;
  }

  const payload = {
    ...this.accountForm.getRawValue()
  };

  this.accountService.updateProfile(payload).subscribe({
    next: () => {
      this.successMessage = 'Changes saved successfully!';
      this.errorMessage = '';
      this.cdr.detectChanges();

      setTimeout(() => {
        this.successMessage = '';
      }, 3000);
    },
    error: () => {
      this.errorMessage = 'Failed to save changes.';
      this.successMessage = '';
    }
  });
}

getImageUrl(filename: string | null): string | null {
  if (!filename) return null;
  return `http://localhost:8080/api/profile/picture/${filename}`;
}



    onChangePassword(): void {
      this.router.navigate(['/change-password']);
    }
onFileSelected(event: any) {
  const file = event.target.files?.[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = () => {
    this.profilePreview = reader.result as string;
    this.cdr.detectChanges();
  };
  reader.readAsDataURL(file);

  const formData = new FormData();
  formData.append('file', file);

  this.accountService.uploadPicture(formData).subscribe(res => {
    this.profilePreview =
      `http://localhost:8080/api/profile/picture/${res.picture}`;
  });
}
  }
