import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { firstValueFrom, of } from 'rxjs';
import { catchError, take, timeout } from 'rxjs/operators';

import { GeocodingService } from '../../logedin.homepage/services/geocoding.service';

export type StopRideResult = {
  stoppedAtIso: string;
  lat: number;
  lng: number;
  stopAddress: string;
  newPrice: number;
};

@Component({
  selector: 'app-stop-ride',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stop-ride.component.html',
  styleUrls: ['./stop-ride.component.css'],
})
export class StopRideComponent {
  @Input({ required: true }) rideId!: number;
  @Input() currentPrice: number | null = null;

  @Output() close = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<StopRideResult>();

  loading = false;
  errorMsg = '';

  stopTimeIso: string | null = null;
  lat: number | null = null;
  lng: number | null = null;
  stopAddress: string | null = null;
  newPrice: number | null = null;

  constructor(private geocoding: GeocodingService) {}

  onClose() {
    if (this.loading) return;
    this.close.emit();
  }

  async captureStop() {
    this.errorMsg = '';
    this.loading = true;

    try {
      const pos = await this.getCurrentPosition();
      this.lat = pos.coords.latitude;
      this.lng = pos.coords.longitude;

      const now = new Date();
      this.stopTimeIso = now.toISOString();

      // reverse geocode (sa timeout-om + fallback)
      const loc = await firstValueFrom(
        this.geocoding.reverse(this.lat, this.lng).pipe(
          take(1),
          timeout(5000),
          catchError(() => of(null))
        )
      );

      this.stopAddress =
        (loc as any)?.address ??
        `Lat ${this.lat.toFixed(5)}, Lng ${this.lng.toFixed(5)}`;

      // UI-only proračun cene (privremeno)
      const base = 250;
      const old = this.currentPrice ?? 800;
      this.newPrice = Math.max(base, Math.round(old * 0.75));
    } catch (e: any) {
      this.errorMsg =
        e?.message ||
        'Ne mogu da pristupim lokaciji. Proveri browser permission (Location) i probaj opet.';
    } finally {
      this.loading = false;
    }
  }

  confirmStop() {
    if (this.loading) return;

    if (
      !this.stopTimeIso ||
      this.lat == null ||
      this.lng == null ||
      !this.stopAddress ||
      this.newPrice == null
    ) {
      this.errorMsg = 'Prvo klikni “Capture stop data” da uzmemo lokaciju i vreme.';
      return;
    }

    this.confirm.emit({
      stoppedAtIso: this.stopTimeIso,
      lat: this.lat,
      lng: this.lng,
      stopAddress: this.stopAddress,
      newPrice: this.newPrice,
    });
  }

  private getCurrentPosition(): Promise<GeolocationPosition> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation nije podržan u ovom browseru.'));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (pos) => resolve(pos),
        (err) => reject(new Error(err.message || 'User denied Geolocation')),
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 30000 }
      );
    });
  }
}
