import { ChangeDetectorRef, Component , OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
  FormGroup,
} from '@angular/forms';
import { AuthService } from '../../auth/services/auth.service';

interface RatingRequestDTO {
  vehicleRating: number;    // 1-5
  driverRating: number;
  comment: string;
  rideId: number | null;
  username: string;

}


@Component({
  selector: 'app-grading.component',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './grading.component.html',
  styleUrl: './grading.component.css',
})
export class GradingComponent implements OnInit {
  form: FormGroup;
  rideId!: number;

  isSending = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(private fb: FormBuilder,
     private http: HttpClient,
      private route: ActivatedRoute,
      private auth: AuthService,
      private cdr: ChangeDetectorRef
    ) {
    this.form = this.fb.group({
      vehicleRating: [null, [Validators.required, Validators.min(1), Validators.max(5)]],
      driverRating: [null, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: [''],
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('rideId');
    const parsed = Number(idParam);

    this.rideId = Number.isFinite(parsed) ? parsed : 0;

    if (!this.rideId) {
      this.errorMessage = 'Invalid rideId in URL.';
    }

    console.log('rideId =', this.rideId);
  }

  submit(): void {
    this.errorMessage = null;
    this.successMessage = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage = 'Fill mandatory fields (ratings 1–5).';
      return;
    }

    const username = this.auth.getUsername();
    if (!username) {
      this.errorMessage = 'You must be logged in to submit a rating.';
      return;
    }

    const payload: RatingRequestDTO = {
      vehicleRating: Number(this.form.value.vehicleRating),
      driverRating: Number(this.form.value.driverRating),
      comment: (this.form.value.comment ?? '').trim(),
      rideId: this.rideId,
      username: username,
    };

    this.isSending = true;

    this.http.post('http://localhost:8080/api/reviews', payload).subscribe({
      next: () => {
        this.isSending = false;
        this.successMessage = 'Rating submitted successfully.';
        this.form.reset();
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.isSending = false;

        const backendMsg =
          (typeof err.error === 'string' && err.error) ||
          err.error?.message ||
          err.error?.error ||
          null;

        if (err.status === 0) {
          this.errorMessage = 'Cannot connect to server (network/CORS).';
        } else if (err.status === 400) {
          this.errorMessage = backendMsg ?? 'Bad request.';
        } else if (err.status === 401) {
          this.errorMessage = 'You do not have permission to send a review.';
        } else if (err.status === 403) {
          this.errorMessage = 'You cant rate this ride (time limit is 3 days).';
        } else if (err.status === 409) {
          this.errorMessage = 'You have already submitted a review for this ride.';
        } else {
          this.errorMessage = backendMsg ?? 'Error sending review. Please try again.';
        }
        this.cdr.detectChanges();
      },
    });
  }

  
  hasError(controlName: string, error: string): boolean {
    const c = this.form.get(controlName);
    return !!c && c.touched && c.hasError(error);
  }
}
