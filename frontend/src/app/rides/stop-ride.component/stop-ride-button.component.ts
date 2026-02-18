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

    const ok = confirm('Da li ste sigurni? Vožnja će biti završena na trenutnoj lokaciji.');
    if (!ok) return;

    this.loading.set(true);
    this.error.set('');
    this.response.set(null);

    navigator.geolocation.getCurrentPosition(
      (pos) => {

        // Backend prima LocalDateTime, zato skidamo "Z"
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
              this.error.set('Nemate dozvolu da zaustavite ovu vožnju.');
            } else if (err?.status === 409) {
              this.error.set('Vožnja nije u toku.');
            } else if (err?.status === 401) {
              this.error.set('Niste ulogovani.');
            } else {
              this.error.set('Greška pri zaustavljanju vožnje.');
            }

          },
          complete: () => this.loading.set(false),
        });

      },
      (geoError) => {
        this.loading.set(false);

        if (geoError?.code === 1) {
          this.error.set('GPS dozvola nije odobrena.');
        } else {
          this.error.set('Ne mogu da dobijem lokaciju.');
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000
      }
    );
  }
}
