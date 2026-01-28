import { AfterViewInit, ChangeDetectorRef, Component, OnDestroy, Output, EventEmitter  } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RideOrderService, VehicleType } from '../services/ride-order.service';
import { GeocodingService } from '../services/geocoding.service';
import { LocationModel } from '../models/location.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css'],
})
export class MapComponent implements AfterViewInit, OnDestroy {
  @Output() routeEstimated = new EventEmitter<number>();

  private subs = new Subscription();
  private map: any;
  private L: any;

  private fromMarker: any = null;
  private toMarker: any = null;
  private stopMarkers: any[] = [];
  private routeControl: any = null;

  etaText: string = '';
  distanceKm: number | null = null;
  durationMin: number | null = null;
  priceRsd: number | null = null;

  private currentVehicleType: VehicleType = 'standard';

  private clickStep: 0 | 1 | 2 = 0;

  constructor(
    private rideOrder: RideOrderService,
    private geocoding: GeocodingService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  async ngAfterViewInit(): Promise<void> {
    if (typeof window === 'undefined') return;

    const leaflet = await import('leaflet');
    (window as any).L = leaflet;
    await import('leaflet-routing-machine');
    this.L = (window as any).L;

    this.initMap();
    this.listenToLocations();
    this.enableClickToPick();
  }

  private initMap() {
    const noviSad: [number, number] = [45.2671, 19.8335];

    this.map = this.L.map('map', {
      center: noviSad,
      zoom: 13,
    });

    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    setTimeout(() => {
      this.map.invalidateSize();
    }, 300);
  }

  private listenToLocations() {
    this.subs.add(
      this.rideOrder.from$.subscribe((from) => {
        this.updateMarker('from', from);
        this.updateRoute();
      })
    );

    this.subs.add(
      this.rideOrder.to$.subscribe((to) => {
        this.updateMarker('to', to);
        this.updateRoute();
      })
    );

    this.subs.add(
      this.rideOrder.stops$.subscribe((stops) => {
        this.updateStopMarkers(stops);
        this.updateRoute();
      })
    );


    this.subs.add(
      this.rideOrder.vehicleType$.subscribe((t) => {
        this.currentVehicleType = t;
        if (this.distanceKm != null) {
          this.priceRsd = this.calculatePrice(this.distanceKm, t);
          this.cdr.detectChanges();
        }
      })
    );
  }

  private redPinIcon() {
    return this.L.icon({
      iconUrl: '/images/leaflet/marker-icon.png',
      iconRetinaUrl: '/images/leaflet/marker-icon-2x.png',
      shadowUrl: '/images/leaflet/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41],
    });
  }

  private updateMarker(which: 'from' | 'to', loc: LocationModel | null) {
    if (!loc) {
      if (which === 'from' && this.fromMarker) {
        this.map.removeLayer(this.fromMarker);
        this.fromMarker = null;
      }
      if (which === 'to' && this.toMarker) {
        this.map.removeLayer(this.toMarker);
        this.toMarker = null;
      }
      return;
    }

    const marker = this.L.marker([loc.lat, loc.lng], {
      icon: this.redPinIcon(),
    });

    if (which === 'from') {
      if (this.fromMarker) this.map.removeLayer(this.fromMarker);
      this.fromMarker = marker.addTo(this.map).bindPopup('From').openPopup();
    } else {
      if (this.toMarker) this.map.removeLayer(this.toMarker);
      this.toMarker = marker.addTo(this.map).bindPopup('To').openPopup();
    }
  }

    private updateStopMarkers(stops: LocationModel[]) {

    this.stopMarkers.forEach(m => this.map.removeLayer(m));
    this.stopMarkers = [];

    stops.forEach((s, index) => {
      const marker = this.L.marker([s.lat, s.lng], {
        icon: this.redPinIcon(),
      })
        .addTo(this.map)
        .bindPopup(`Stop ${index + 1}`);

      this.stopMarkers.push(marker);
    });
  }


  private calculatePrice(distanceKm: number, type: VehicleType): number {
    const basePriceMap: Record<VehicleType, number> = {
      standard: 250,
      luxury: 450,
      van: 350,
    };

    const base = basePriceMap[type];
    const perKm = 120;

    return Math.round(base + distanceKm * perKm);
  }

  private updateRoute() {
    const from = this.rideOrder.getFrom();
    const to = this.rideOrder.getTo();
    const stops = this.rideOrder.getStops();

    if (this.routeControl) {
      this.map.removeControl(this.routeControl);
      this.routeControl = null;
    }

    if (!(this.L as any).Routing) {
      console.warn('Leaflet Routing is not available!');
      return;
    }

    if (!from || !to) {
      this.etaText = '';
      this.distanceKm = null;
      this.durationMin = null;
      this.priceRsd = null;
      this.updateStopMarkers([]);
      
      this.cdr.detectChanges();
      return;
    }

    const waypoints = [
      this.L.latLng(from.lat, from.lng),
      ...stops.map((s: LocationModel) => this.L.latLng(s.lat, s.lng)),
      this.L.latLng(to.lat, to.lng),
    ];

    this.routeControl = (this.L as any).Routing.control({
      waypoints,
      router: (this.L as any).Routing.osrmv1({
        serviceUrl: 'https://router.project-osrm.org/route/v1',
      }),
      show: false,
      addWaypoints: false,
      draggableWaypoints: false,
      fitSelectedRoutes: true,
      lineOptions: {
        styles: [{ weight: 6, opacity: 0.8 }],
      },
      createMarker: () => null,
    }).addTo(this.map);

    this.routeControl.on('routesfound', (e: any) => {
      const route = e.routes?.[0];
      if (!route) return;

      const seconds = route.summary.totalTime;
      const meters = route.summary.totalDistance;

      const minutes = Math.round(seconds / 60);
      const km = Number((meters / 1000).toFixed(1));

      this.durationMin = minutes;
      this.distanceKm = km;
      this.priceRsd = this.calculatePrice(km, this.currentVehicleType);

      this.etaText = `Estimated time: ${minutes} min · Distance: ${km} km · Price: ${this.priceRsd} RSD`;
      this.cdr.detectChanges();

      this.routeEstimated.emit(minutes);


    });
  }

  private enableClickToPick() {
    this.map.on('click', (e: any) => {
      const lat = e.latlng.lat;
      const lng = e.latlng.lng;

      if (this.clickStep === 0 || this.clickStep === 2) {
        this.clickStep = 1;

        this.geocoding.reverse(lat, lng).subscribe((loc) => {
          this.rideOrder.setFrom(loc);
        });

        return;
      }

      if (this.clickStep === 1) {
        this.clickStep = 2;

        this.geocoding.reverse(lat, lng).subscribe((loc) => {
          this.rideOrder.setTo(loc);
        });

        return;
      }
    });
  }
}
