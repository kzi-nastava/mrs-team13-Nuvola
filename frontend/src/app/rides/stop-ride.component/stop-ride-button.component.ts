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

    // Zahtev traži da aplikacija "kupi podatke o mestu i vremenu"
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const time = new Date().toISOString();
        this.lastStopInfo.set({
          time,
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
        });

        // Backend trenutno ne prima lokaciju/vreme u body-u (stub), ali mi ih skupljamo u FE.
        this.api.stopRide(this.rideId).subscribe({
          next: (res) => this.response.set(res),
          error: () => this.error.set('Greška pri zaustavljanju vožnje. Proveri da li backend radi i da li rideId postoji.'),
          complete: () => this.loading.set(false),
        });
      },
      () => {
        this.loading.set(false);
        this.error.set('Ne mogu da dobijem lokaciju (GPS permission).');
      }
    );
  }
}
