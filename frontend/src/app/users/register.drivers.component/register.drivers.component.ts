import { Component, ChangeDetectorRef, ElementRef, ViewChild  } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-register.drivers.component',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.drivers.component.html',
  styleUrl: './register.drivers.component.css',
})
export class RegisterDriversComponent {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  private toastTimer: any;
  successToast: boolean = false;
  driverForm: FormGroup;
  profilePreview: string | null = null;

  // REGEX patterns
  private namePattern = /^[A-ZŠĐČĆŽ][a-zšđčćž]+(?:[ -][A-ZŠĐČĆŽ][a-zšđčćž]+)*$/;
  private addressPattern = /^[A-ZŠĐČĆŽ][A-Za-zŠĐČĆŽšđčćž0-9\s,.\-\/]{2,}$/;
  private phonePattern = /^\+?[0-9\s]{6,20}$/;
  private modelPattern = /^[A-Za-z0-9\s\-]{2,30}$/;
  private platePattern = /^[A-Z]{2}-\d{3,4}-[A-Z]{2}$/;

  constructor(private fb: FormBuilder, private cdr: ChangeDetectorRef ) {
    this.driverForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.pattern(this.namePattern)]],
      lastName: ['', [Validators.required, Validators.pattern(this.namePattern)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(this.phonePattern)]],
      homeAddress: ['', [Validators.required, Validators.pattern(this.addressPattern)]],

      model: ['', [Validators.required, Validators.pattern(this.modelPattern)]],
      type: ['Standard', Validators.required],
      registrationNumber: ['', [Validators.required, Validators.pattern(this.platePattern)]],
      seats: [4, [Validators.required, Validators.min(4), Validators.max(10)]],

      babiesAllowed: [false],
      petsAllowed: [false],
      profilePicture: [null],
    });
  }

onFileSelected(event: any) {
  const file = event.target.files?.[0];
  if (!file) return;

  if (!file.type.startsWith('image/')) {
    return;
  }

  this.driverForm.patchValue({ profilePicture: file });

  const reader = new FileReader();
  reader.onload = () => {
    this.profilePreview = reader.result as string;
    this.cdr.detectChanges(); 
  };
  reader.readAsDataURL(file);
}

onSubmit() {
  if (this.driverForm.invalid) {
    this.driverForm.markAllAsTouched();
    return;
  }

  this.successToast = true;

  // reset form fields
  this.driverForm.reset({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    homeAddress: '',
    model: '',
    type: 'Standard',
    registrationNumber: '',
    seats: 4,
    babiesAllowed: false,
    petsAllowed: false,
    profilePicture: null,
  });

   this.profilePreview = null;
  if (this.fileInput) {
    this.fileInput.nativeElement.value = '';
  }

  this.driverForm.markAsPristine();
  this.driverForm.markAsUntouched();

  if (this.toastTimer) {
    clearTimeout(this.toastTimer);
  }

  this.toastTimer = setTimeout(() => {
    this.successToast = false;
    this.cdr.detectChanges(); 
  }, 2000);
}

}
