import { Component, Input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RideApiService, CreatedRideDTO } from '../service/ride-api.service';

@Component({
  selector: 'app-stop-ride-button',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stop-ride-button.component.html',
  styleUrls: ['./stop-ride-button.component.css'],
})
export class StopRideButtonComponent {

  @Input({ required: true }) rideId!: number;

  loading = signal(false);
  error = signal('');
  lastStopInfo = signal<{ time: string; lat: number; lng: number } | null>(null);
  response = signal<CreatedRideDTO | null>(null);

  constructor(private api: RideApiService) {}

  stopRide() {

    const ok = confirm('Are you sure? The ride will be stopped at this location.');
    if (!ok) return;

    this.loading.set(true);
    this.error.set('');
    this.response.set(null);

    navigator.geolocation.getCurrentPosition(
      (pos) => {


        const stoppedAt = new Date().toISOString().slice(0, 19);

        const payload = {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
          stoppedAt: stoppedAt
        };

        this.lastStopInfo.set({
          time: stoppedAt,
          lat: payload.lat,
          lng: payload.lng
        });

        this.api.stopRide(this.rideId, payload).subscribe({
          next: (res) => {
            this.response.set(res);
          },
          error: (err) => {

            if (err?.status === 403) {
              this.error.set('You cant stop the ride');
            } else if (err?.status === 409) {
              this.error.set('Ride is not in progress');
            } else if (err?.status === 401) {
              this.error.set('Youre not login');
            } else {
              this.error.set('Error for stopping ride');
            }

          },
          complete: () => this.loading.set(false),
        });

      },
      (geoError) => {
        this.loading.set(false);

        if (geoError?.code === 1) {
          this.error.set('GPS is not allowed');
        } else {
          this.error.set('I cant find the location');
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000
      }
    );
  }
}
