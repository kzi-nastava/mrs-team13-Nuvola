import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RideService } from '../service/ride.service';
import { RideModel } from '../model/ride.model';

type Role = 'DRIVER' | 'PASSENGER';

@Component({
  selector: 'app-cancel-ride.component',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cancel-ride.component.html',
  styleUrl: './cancel-ride.component.css',
})

export class CancelRideComponent implements OnInit {
  role = signal<Role>('PASSENGER'); // default

  rideId!: number;
  ride = signal<RideModel | undefined>(undefined);

  message = signal<string | null>(null);

  driverForm = new FormGroup({
    reason: new FormControl('', [Validators.required, Validators.minLength(5)]),
  });

  canDriverCancel = computed(() => {
    const r = this.ride();
    return r ? this.rideService.canDriverCancel(r.id) : false;
  });

  canPassengerCancel = computed(() => {
    const r = this.ride();
    return r ? this.rideService.canPassengerCancel(r.id) : false;
  });

  constructor(private route: ActivatedRoute, private rideService: RideService) {}

  ngOnInit(): void {
    this.rideId = Number(this.route.snapshot.paramMap.get('id'));
    this.ride.set(this.rideService.getRideById(this.rideId));

    // ✅ privremeno: uloga iz query param
    // /rides/1/cancel?role=driver  ili  ?role=passenger
    const roleParam = (this.route.snapshot.queryParamMap.get('role') || '').toLowerCase();
    this.role.set(roleParam === 'driver' ? 'DRIVER' : 'PASSENGER');
  }

  get reason() {
    return this.driverForm.controls.reason;
  }
  cancelDriver() {
    this.message.set(null);

    if (this.driverForm.invalid) {
      this.driverForm.markAllAsTouched();
      return;
    }

    const res = this.rideService.cancelByDriver(this.rideId, this.reason.value ?? '');
    this.message.set(res.message);
    this.ride.set(this.rideService.getRideById(this.rideId));
  }

  cancelPassenger() {
    this.message.set(null);

    const res = this.rideService.cancelByPassenger(this.rideId);
    this.message.set(res.message);
    this.ride.set(this.rideService.getRideById(this.rideId));
  }
}
