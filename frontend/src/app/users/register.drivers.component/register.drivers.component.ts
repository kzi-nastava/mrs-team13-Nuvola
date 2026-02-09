import { Component, ChangeDetectorRef, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DriverService } from '../../services/driver.service';


@Component({
  selector: 'app-register.drivers.component',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.drivers.component.html',
  styleUrl: './register.drivers.component.css',
})
export class RegisterDriversComponent {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  driverForm: FormGroup;
  profilePreview: string | null = null;
  successToast = false;
  private toastTimer: any;

  // REGEX patterns
  private namePattern = /^[A-ZŠĐČĆŽ][a-zšđčćž]+(?:[ -][A-ZŠĐČĆŽ][a-zšđčćž]+)*$/;
  private addressPattern = /^[A-ZŠĐČĆŽ][A-Za-zŠĐČĆŽšđčćž0-9\s,.\-\/]{2,}$/;
  private phonePattern = /^\+?[0-9\s]{6,20}$/;
  private modelPattern = /^[A-Za-z0-9\s\-]{2,30}$/;
  private platePattern = /^[A-Z]{2}-\d{3,4}-[A-Z]{2}$/;

  constructor(private fb: FormBuilder, private cdr: ChangeDetectorRef, private driverService: DriverService) {
    this.driverForm = this.fb.group({
      // driver
      firstName: ['', [Validators.required, Validators.pattern(this.namePattern)]],
      lastName: ['', [Validators.required, Validators.pattern(this.namePattern)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(this.phonePattern)]],
      address: ['', [Validators.required, Validators.pattern(this.addressPattern)]],
      picture: [null],

      // vehicle
      model: ['', [Validators.required, Validators.pattern(this.modelPattern)]],
      type: ['STANDARD', Validators.required],
      regNumber: ['', [Validators.required, Validators.pattern(this.platePattern)]],
      numOfSeats: [4, [Validators.required, Validators.min(4), Validators.max(10)]],
      babyFriendly: [false],
      petFriendly: [false],
    });

      this.driverForm.get('email')?.valueChanges.subscribe(() => {
    const control = this.driverForm.get('email');
    if (control?.hasError('alreadyExists')) {
      control.setErrors(null);
    }
  });

  this.driverForm.get('regNumber')?.valueChanges.subscribe(() => {
    const control = this.driverForm.get('regNumber');
    if (control?.hasError('alreadyExists')) {
      control.setErrors(null);
    }
  });
  }

onFileSelected(event: any) {
  const file = event.target.files?.[0];
  if (!file || !file.type.startsWith('image/')) return;

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

  const { picture, ...payload } = this.driverForm.value;

  this.driverService.createDriver(payload).subscribe({
    next: (createdDriver) => {

      const fileInputEl = this.fileInput.nativeElement;
      const file = fileInputEl.files?.[0];

      if (file && createdDriver?.id) {
        const formData = new FormData();
        formData.append('file', file);

        this.driverService
           .uploadDriverPicture(createdDriver.id, formData)
          .subscribe();
      }

      this.showToast('success');

      this.driverForm.reset({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        address: '',
        model: '',
        type: 'STANDARD',
        regNumber: '',
        numOfSeats: 4,
        babyFriendly: false,
        petFriendly: false,
      });

      this.profilePreview = null;
      this.fileInput.nativeElement.value = '';
    },

    error: (err) => {
      const code = err?.error?.code;

      if (code === 'EMAIL_ALREADY_EXISTS') {
        this.driverForm.get('email')?.setErrors({ alreadyExists: true });
        return;
      }

      if (code === 'REG_NUMBER_ALREADY_EXISTS') {
        this.driverForm.get('regNumber')?.setErrors({ alreadyExists: true });
        return;
      }

      console.error('Unexpected error', err);
    }
  });
}



private showToast(type: 'success' | 'email' | 'reg', duration = 2500) {
  this.successToast = false;

  if (type === 'success') this.successToast = true;

  if (this.toastTimer) clearTimeout(this.toastTimer);
  this.toastTimer = setTimeout(() => {
    this.successToast = false;
    this.cdr.detectChanges();
  }, duration);
}


}
