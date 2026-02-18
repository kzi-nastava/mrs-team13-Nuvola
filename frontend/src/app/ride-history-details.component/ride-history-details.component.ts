import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
  ViewChild,
  ElementRef,
  AfterViewInit,
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import * as L from 'leaflet';
import { RideHistoryService } from '../homepage/service/ride-history.service';
import { RideHistoryDetailsDTO } from '../history.ride.registereduser.component/model/ride-history.model';
import { RideApiService } from '../rides/service/ride-api.service';

@Component({
  selector: 'app-ride-history-details',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ride-history-details.component.html',
  styleUrls: ['./ride-history-details.component.css'],
})
export class RideHistoryDetailsComponent implements OnChanges, AfterViewInit {
  @Input() rideId?: number;
  @Output() closed = new EventEmitter<void>();

  @ViewChild('mapEl') mapEl?: ElementRef<HTMLDivElement>;

  loading = false;
  error?: string;
  details?: RideHistoryDetailsDTO;

  scheduleMode: 'now' | 'later' = 'now';
  scheduledTime?: string;

  reorderLoading = false;  
  reorderSuccess = false;

  private map?: L.Map;
  private layer?: L.LayerGroup;
  private viewReady = false;

  constructor(
    private rideHistory: RideHistoryService,
    private rideApiService: RideApiService,
    private cdr: ChangeDetectorRef
  ) {
    // Fix za Leaflet ikone
    delete (L.Icon.Default.prototype as any)._getIconUrl;
    L.Icon.Default.mergeOptions({
      iconRetinaUrl:
        'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
      iconUrl:
        'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
      shadowUrl:
        'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    });
  }

  ngAfterViewInit(): void {
    console.log('ngAfterViewInit');
    this.viewReady = true;
    this.cdr.detectChanges();
    this.initMapIfReady();
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log('ngOnChanges', changes);
    if (changes['rideId'] && this.rideId) {
      this.resetMap();
      this.fetchDetails(this.rideId);
    }
  }

  //private fetchDetails(id: number): void {
    //console.log('Fetching details for ride:', id);
//    this.loading = true;
  //  this.error = undefined;
    //this.details = undefined;

//    this.rideHistory.getHistoryDetails(id).subscribe({
  //    next: (res) => {
    //    console.log('Details loaded:', res);
  
      //  this.details = res;
        //this.loading = false;
        //this.cdr.detectChanges();
        //setTimeout(() => this.initMapIfReady(), 100);
      //},
      //error: (err) => {
        //console.error('Error loading details:', err);
        //this.loading = false;
        //this.error =
         // err?.error?.message ||
          //err?.error?.error ||
          //'Greška pri učitavanju detalja.';
      //},
    //});
 // }


 private fetchDetails(id: number): void {
  this.loading = true;
  this.error = undefined;
  this.details = undefined;

  this.rideHistory.getHistoryDetails(id).subscribe({
    next: (res) => {
      console.log('✅ Driver info:', res.driver);
      this.details = res;
      this.loading = false;
      this.cdr.detectChanges();
      setTimeout(() => this.initMapIfReady(), 100);
    },
    error: (err) => {
      console.error('Error:', err);
      this.loading = false;
      this.error = err?.error?.message || 'Greška pri učitavanju detalja.';
    },
  });
}

reorder(): void {
  if (!this.details?.routeId) {
    this.error = '❌ Route ID nije dostupan.';
    return;
  }

  if (this.scheduleMode === 'later' && !this.scheduledTime) {
    this.error = '❌ Molimo odaberite vreme.';
    return;
  }

  this.reorderLoading = true;
  this.error = undefined;
  this.reorderSuccess = false;

  const payload = {
    routeId: this.details.routeId,
    scheduledTime:
      this.scheduleMode === 'later' ? this.scheduledTime : null,
  };

  
  this.rideApiService.reorderRide(payload).subscribe({
    next: (res: any) => {
      console.log('✅ Ride successfully reordered:', res);
      this.reorderLoading = false;
      this.reorderSuccess = true;
      this.cdr.detectChanges();

      setTimeout(() => {
        this.close();
      }, 3000);
    },
    error: (err: any) => {
      console.error('❌ Reorder error:', err);
      this.reorderLoading = false;
      this.error =
        err?.error?.message ||
        err?.error?.error ||
        'Greška pri naručivanju voznje.';
      this.cdr.detectChanges();
    },
  });
}


  private initMapIfReady(): void {
  console.log('=== initMapIfReady START ===');
  console.log('viewReady:', this.viewReady);
  console.log('details:', !!this.details);
  console.log('mapEl:', this.mapEl);
  console.log('mapEl?.nativeElement:', this.mapEl?.nativeElement);

  if (!this.viewReady) {
    console.log('❌ STOP: viewReady is false');
    return;
  }

  if (!this.details) {
    console.log('❌ STOP: no details');
    return;
  }

  if (!this.mapEl?.nativeElement) {
    console.log('❌ STOP: no mapEl element found');
    console.log('mapEl object:', this.mapEl);
    return;
  }

  console.log('✅ All checks passed');

  const points = this.extractRoutePoints(this.details);
  console.log('Extracted points:', points);

  if (!points.length) {
    console.log('❌ No points extracted');
    this.error = 'Nema validnih koordinata za prikaz rute.';
    return;
  }

  this.resetMap();

  setTimeout(() => {
    try {
      const el = this.mapEl!.nativeElement;
      const first = points[0];

      console.log('🗺️ Map container:', el);
      console.log('🗺️ Container size:', {
        width: el.offsetWidth,
        height: el.offsetHeight,
        display: window.getComputedStyle(el).display,
      });

      this.map = L.map(el, {
        center: [first.latitude, first.longitude],
        zoom: 13,
        zoomControl: true,
      });

      console.log('🗺️ Map object created:', !!this.map);

      // Tile layer
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19,
      }).addTo(this.map);

      this.layer = L.layerGroup().addTo(this.map);

      const latlngs: L.LatLngExpression[] = points.map((p) => [
        p.latitude,
        p.longitude,
      ]);

      // 🟢 PICKUP marker (zelena)
      L.marker(latlngs[0], {
        icon: L.icon({
          iconUrl:
            'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
          shadowUrl:
            'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
          iconSize: [25, 41],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34],
          shadowSize: [41, 41],
        }),
      })
        .addTo(this.layer)
        .bindPopup('<strong>🟢 Pickup</strong><br>' + this.details!.pickup);

      // 🔴 DROPOFF marker (crvena)
      L.marker(latlngs[latlngs.length - 1], {
        icon: L.icon({
          iconUrl:
            'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
          shadowUrl:
            'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
          iconSize: [25, 41],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34],
          shadowSize: [41, 41],
        }),
      })
        .addTo(this.layer)
        .bindPopup('<strong>🔴 Dropoff</strong><br>' + this.details!.dropoff);

      // Polyline sa boljim stilom
      const poly = L.polyline(latlngs, {
        color: '#0066cc',
        weight: 3,
        opacity: 0.8,
        dashArray: '5, 5', // Optional: crtka ako želiš
        lineCap: 'round',
        lineJoin: 'round',
      }).addTo(this.layer);

      // Fit bounds sa padding
      const bounds = poly.getBounds();
      this.map.fitBounds(bounds, {
        padding: [50, 50],
        maxZoom: 15,
      });

      setTimeout(() => {
        this.map?.invalidateSize();
        console.log('✅ MAP INITIALIZED SUCCESSFULLY!');
      }, 100);
    } catch (error) {
      console.error('❌ Map initialization error:', error);
      this.error = 'Greška pri inicijalizaciji mape.';
    }
  }, 200);
}


  private extractRoutePoints(
    details: RideHistoryDetailsDTO
  ): Array<{ latitude: number; longitude: number }> {
    console.log('Parsing coordinates:', {
      pickup: details.pickup,
      dropoff: details.dropoff,
    });

    const pickupStr = details?.pickup?.trim();
    const dropoffStr = details?.dropoff?.trim();

    if (!pickupStr || !dropoffStr) {
      console.log('❌ Missing coordinates');
      return [];
    }

    const parsePoint = (str: string): { latitude: number; longitude: number } | null => {
      // Format: "lat , lng" (sa spacingom)
      const parts = str.split(',').map((p) => p.trim());

      if (parts.length !== 2) {
        console.log('❌ Invalid format:', str);
        return null;
      }

      const lat = parseFloat(parts[0]);
      const lng = parseFloat(parts[1]);

      if (isNaN(lat) || isNaN(lng)) {
        console.log('❌ NaN values:', { lat, lng });
        return null;
      }

      console.log('✅ Parsed point:', { lat, lng });
      return { latitude: lat, longitude: lng };
    };

    const pickup = parsePoint(pickupStr);
    const dropoff = parsePoint(dropoffStr);

    if (!pickup || !dropoff) {
      console.log('❌ Failed to parse coordinates');
      return [];
    }

    return [pickup, dropoff];
  }

  private resetMap(): void {
    if (this.map) {
      this.map.remove();
      this.map = undefined;
    }
    this.layer = undefined;
  }

  close(): void {
    this.resetMap();
    this.closed.emit();
  }
}
