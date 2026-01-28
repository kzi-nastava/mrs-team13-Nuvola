import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { GeocodingService } from '../../logedin.homepage/services/geocoding.service';
import { RideOrderService } from '../../logedin.homepage/services/ride-order.service';
import { LocationModel } from '../../logedin.homepage/models/location.model';
import { MapComponent } from '../../logedin.homepage/map.component/map.component';

@Component({
  selector: 'app-estimate-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MapComponent],
  templateUrl: './estimate.form.component.html',
  styleUrls: ['./estimate.form.component.css'],
})
export class EstimateFormComponent {
  estimatedMinutes: number | null = null;

  form = new FormGroup({
    pickup: new FormControl('', [Validators.required, Validators.minLength(3)]),
    destination: new FormControl('', [Validators.required, Validators.minLength(3)]),
  });

  constructor(
    private geocoding: GeocodingService,
    private rideOrder: RideOrderService
  ) {}

  get pickup() {
    return this.form.controls.pickup;
  }

  get destination() {
    return this.form.controls.destination;
  }

  // prima iz MapComponent: (routeEstimated)="onRouteEstimated($event)"
  onRouteEstimated(minutes: number) {
    this.estimatedMinutes = minutes;
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // resetuj prikaz dok racuna
    this.estimatedMinutes = null;

    const pickupText = this.pickup.value!.trim();
    const destinationText = this.destination.value!.trim();

    // 1) From
    this.geocoding.search(pickupText + ' Novi Sad').subscribe((res: LocationModel[]) => {
      if (res.length > 0) {
        this.rideOrder.setFrom(res[0]);
      }
    });

    // 2) To
    this.geocoding.search(destinationText + ' Novi Sad').subscribe((res: LocationModel[]) => {
      if (res.length > 0) {
        this.rideOrder.setTo(res[0]);
      }
    });
  }
}
