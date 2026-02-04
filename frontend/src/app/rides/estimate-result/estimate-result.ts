import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import 'leaflet/dist/leaflet.css';
import 'leaflet-defaulticon-compatibility';
import 'leaflet-defaulticon-compatibility/dist/leaflet-defaulticon-compatibility.css';



import * as L from 'leaflet';

import { RideEstimateService } from '../service/ride-estimate.service';
import { RideEstimateResponseDTO } from '../model/ride-estimate.model';

@Component({
  selector: 'app-estimate-result',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './estimate-result.component.html',
  styleUrls: ['./estimate-result.component.css'],
})
export class EstimateResultComponent implements OnInit {
  loading = true;
  error: string | null = null;
  result: RideEstimateResponseDTO | null = null;

  private map!: L.Map;
  private routeLayer?: L.GeoJSON;
  private routeDrawn = false;

  private pickup = '';
  private destination = '';

  constructor(
    private route: ActivatedRoute,
    private rideEstimateService: RideEstimateService,
    private cdr: ChangeDetectorRef
  ) {}

  // =========================
  // FORSIRAJ NOVI SAD
  // =========================
  private withCity(addr: string): string {
    const a = (addr || '').trim();
    if (!a) return a;

    const low = a.toLowerCase();
    if (low.includes('novi sad')) return a;

    return `${a}, Novi Sad, Serbia`;
  }

  ngOnInit(): void {
    this.pickup = (this.route.snapshot.queryParamMap.get('pickup') || '').trim();
    this.destination = (this.route.snapshot.queryParamMap.get('destination') || '').trim();

    if (!this.pickup || !this.destination) {
      this.loading = false;
      this.error = 'Nedostaju pickup ili destination parametri u URL-u.';
      return;
    }

    // 1️⃣ Mapa odmah
    this.initMap();

    // 2️⃣ ETA sa backenda
    this.rideEstimateService
      .estimateRide({
        startAddress: this.pickup,
        destinationAddress: this.destination,
      })
      .pipe(finalize(() => {}))
      .subscribe({
        next: (res) => {
          this.result = res;
          this.loading = false;
          this.error = null;

          this.cdr.detectChanges();

          if (!this.routeDrawn) {
            this.routeDrawn = true;
            this.drawRoute().catch(console.error);
          }
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          this.result = null;

          this.error =
            err.error?.message ||
            err.error?.error ||
            'Greška pri proceni vožnje.';

          this.cdr.detectChanges();
          console.error(err);
        },
      });
  }

  private initMap(): void {
    if (this.map) return;

    this.map = L.map('map').setView([45.2671, 19.8335], 12);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    const DefaultIcon = L.Icon.Default as any;
    DefaultIcon.mergeOptions({
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    });
  }

  // =========================
  // GEOCODING (NOVI SAD)
  // =========================
  private async geocode(address: string): Promise<{ lat: number; lon: number }> {
    const q = encodeURIComponent(this.withCity(address));
    const url = `https://nominatim.openstreetmap.org/search?q=${q}&format=json&limit=1`;

    const res = await fetch(url, {
      headers: { 'User-Agent': 'NuvolaApp/1.0' },
    });

    const data = await res.json();
    if (!data || data.length === 0) {
      throw new Error(`Nominatim: adresa nije pronađena: ${address}`);
    }

    return { lat: parseFloat(data[0].lat), lon: parseFloat(data[0].lon) };
  }

  private async drawRoute(): Promise<void> {
    if (!this.map) return;

    const start = await this.geocode(this.pickup);
    const end = await this.geocode(this.destination);

    L.marker([start.lat, start.lon]).addTo(this.map).bindPopup('Start');
    L.marker([end.lat, end.lon]).addTo(this.map).bindPopup('Destination');

    const url =
      `https://router.project-osrm.org/route/v1/driving/` +
      `${start.lon},${start.lat};${end.lon},${end.lat}` +
      `?overview=full&geometries=geojson`;

    const r = await fetch(url);
    if (!r.ok) throw new Error(`OSRM error: ${r.status}`);

    const json = await r.json();
    const geometry = json?.routes?.[0]?.geometry;
    if (!geometry) throw new Error('OSRM: ruta nije pronađena.');

    if (this.routeLayer) this.routeLayer.remove();

    this.routeLayer = L.geoJSON(geometry, { style: { weight: 5 } }).addTo(this.map);
    this.map.fitBounds(this.routeLayer.getBounds(), { padding: [20, 20] });
  }
}
