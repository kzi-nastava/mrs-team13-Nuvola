import {Component,ChangeDetectorRef,ElementRef,ViewChild,OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormBuilder,FormGroup,ReactiveFormsModule,Validators
} from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { DriverService } from '../../services/driver.service';
import { AuthService } from '../service/auth.service';

@Component({
  selector: 'app-driver.account.component',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './driver.account.component.html',
  styleUrl: './driver.account.component.css',
})
export class DriverAccountComponent implements OnInit {
    @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  driverForm!: FormGroup;
  profilePreview: string | null = null;

  successMessage = '';
  errorMessage = '';

  // REGEX 
  private namePattern = /^[A-ZŠĐČĆŽ][a-zšđčćž]+(?:[ -][A-ZŠĐČĆŽ][a-zšđčćž]+)*$/;
  private addressPattern = /^[A-ZŠĐČĆŽ][A-Za-zŠĐČĆŽšđčćž0-9\s,.\-\/]{2,}$/;
  private phonePattern = /^\+?[0-9\s]{6,20}$/;
  private modelPattern = /^[A-Za-z0-9\s\-]{2,30}$/;
  private platePattern = /^[A-Z]{2}-\d{3,4}-[A-Z]{2}$/;

  constructor(
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private driverService: DriverService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadProfile();
  }

  private initForm() {
    this.driverForm = this.fb.group({
      // user
      firstName: ['', [Validators.required, Validators.pattern(this.namePattern)]],
      lastName: ['', [Validators.required, Validators.pattern(this.namePattern)]],
      email: [{ value: '', disabled: true }],
      phone: ['', [Validators.required, Validators.pattern(this.phonePattern)]],
      address: ['', [Validators.required, Validators.pattern(this.addressPattern)]],
      picture: [null],

      // vehicle
      model: ['', [Validators.required, Validators.pattern(this.modelPattern)]],
      type: ['', Validators.required],
      regNumber: [{ value: '', disabled: true }], 
      numOfSeats: ['', [Validators.required, Validators.min(4)]],
      babyFriendly: [false],
      petFriendly: [false],
    });
  }

  private loadProfile() {
    this.driverService.getDriverProfile().subscribe({
      next: (profile) => {
        this.driverForm.patchValue(profile);
        this.profilePreview = this.getImageUrl(profile.picture);
      },
      error: () => {
        this.errorMessage = 'Failed to load profile.';
      }
    });
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

  this.driverService.uploadPicture(formData).subscribe(res => {
    this.profilePreview =
      `http://localhost:8080/api/profile/picture/${res.picture}`;
  });
}


  onSave() {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.driverForm.invalid) {
      this.driverForm.markAllAsTouched();
      return;
    }

    this.driverService.requestProfileChange(this.driverForm.getRawValue())
      .subscribe({
             next: () => {
        this.successMessage =
          'Your changes have been sent to the administrator for approval.';
        this.errorMessage = '';
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to submit profile changes.';
        this.successMessage = '';
        this.cdr.detectChanges();
      }
      });
  }

  onChangePassword() {
    this.router.navigate(['/change-password']);
  }

  private getImageUrl(filename: string | null): string | null {
  if (!filename) return null;
  return `http://localhost:8080/api/profile/picture/${filename}`;
}


}
