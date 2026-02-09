import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-estimate-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './estimate.form.component.html',
  styleUrl: './estimate.form.component.css',
})

export class EstimateFormComponent {
  form = new FormGroup({
    pickup: new FormControl('', [Validators.required, Validators.minLength(3)]),
    destination: new FormControl('', [Validators.required, Validators.minLength(3)]),
  });

  constructor(private router: Router) {}

  get pickup() {
    return this.form.controls.pickup;
  }

  get destination() {
    return this.form.controls.destination;
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const pickup = this.pickup.value!.trim();
    const destination = this.destination.value!.trim();

    this.router.navigate(['/estimate/result'], {
      queryParams: { pickup, destination }
    });
  }

}
