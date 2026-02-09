import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, Inject, OnDestroy, PLATFORM_ID, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs';
import { VehicleType } from '../../logedin.homepage/services/ride-order.service';
import { GeocodingService } from '../../logedin.homepage/services/geocoding.service';
import { RouteDataService } from '../service/route.data.service';
import { LocationModel } from '../../logedin.homepage/models/location.model';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { VehiclesService } from '../service/vehicles.service';
import { VehicleLocationDTO } from '../model/vehicle.location';

@Component({
  selector: 'app-vehicles-map',
  imports: [CommonModule],
  templateUrl: './vehicles.map.html',
  styleUrl: './vehicles.map.css',
})
export class VehiclesMap implements AfterViewInit, OnDestroy {
  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef;
  
  private subs = new Subscription();
  private map: any;
  private L: any;

  private initDone = false;
  private initTimer: any = null;
  private invalidateTimer: any = null;

  private mapClickHandler: any = null;


  private fromMarker: any = null;
  private toMarker: any = null;
  private routeControl: any = null;

  etaText: string = '';
  distanceKm: number | null = null;
  durationMin: number | null = null;
  priceRsd: number | null = null;

  private vehiclesLayer: any = null;
  private vehicleMarkers = new Map<number, any>();

  private currentVehicleType: VehicleType = 'standard';

  private clickStep: 0 | 1 | 2 = 0;

  constructor(
    private routeDataService: RouteDataService,
    private geocoding: GeocodingService,
    private cdr: ChangeDetectorRef,
    private vehiclesService: VehiclesService,
    @Inject(PLATFORM_ID) private platformId: Object

  ) {}

  private carIcon(color: 'green' | 'orange') {
    const fill = color === 'green' ? '#22c55e' : '#f97316';

    return this.L.divIcon({
      className: '',
      iconSize: [26, 26],
      iconAnchor: [13, 13],
      html: `
        <div style="
          width: 26px; height: 26px; border-radius: 999px;
          background: ${fill};
          border: 2px solid white;
          box-shadow: 0 6px 14px rgba(0,0,0,0.20);
          display:flex; align-items:center; justify-content:center;
          font-size: 14px;
        ">🚗</div>
      `,
    });
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    // ocisti sve zakazane callback-e
    if (this.initTimer) cancelAnimationFrame(this.initTimer);
    if (this.invalidateTimer) clearTimeout(this.invalidateTimer);

    // skini event listener
    if (this.map && this.mapClickHandler) {
      this.map.off('click', this.mapClickHandler);
    }

    // ukloni mapu
    if (this.map) {
      this.map.remove();
      this.map = null;
    }

    // resetuj flag (nije obavezno, ali čisto)
    this.initDone = false;
  }

  async ngAfterViewInit(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) return;  // <-- KLJUČNO: nema Leaflet-a na serveru
    if (this.initDone) return;         // <-- guard protiv duplog poziva
    this.initDone = true;

    const leaflet = await import('leaflet');
    this.L = leaflet.default || leaflet;
    (window as any).L = this.L;
    await import('leaflet-routing-machine');

    this.initTimer = requestAnimationFrame(() => {
      this.initMap();
      this.listenToLocations();
      this.enableClickToPick();
      this.loadVehiclesOnce();
    });
  }

  private loadVehiclesOnce(): void {
  this.subs.add(
    this.vehiclesService.getCurrentPositions().subscribe({
      next: (vehicles) => this.renderVehiclesSnapshot(vehicles),
      error: (err) => console.error('Failed to load vehicle positions', err),
    })
  );
}

private renderVehiclesSnapshot(vehicles: VehicleLocationDTO[]): void {
  if (!this.map || !this.L || !this.vehiclesLayer) return;

  const seen = new Set<number>();

  for (const v of vehicles) {
    seen.add(v.vehicleId);

    const color: 'green' | 'orange' = v.occupied ? 'orange' : 'green';
    const existing = this.vehicleMarkers.get(v.vehicleId);

    if (!existing) {
      const marker = this.L.marker([v.latitude, v.longitude], { icon: this.carIcon(color) })
        .addTo(this.vehiclesLayer)
        .bindPopup(`Vehicle: ${v.vehicleId}<br/>Status: ${v.occupied ? 'BUSY' : 'FREE'}`);

      marker.__occupied = v.occupied;
      this.vehicleMarkers.set(v.vehicleId, marker);
    } else {
      existing.setLatLng([v.latitude, v.longitude]);
      const prevOccupied = !!existing.__occupied;
      if (prevOccupied !== v.occupied) existing.setIcon(this.carIcon(color));
      existing.__occupied = v.occupied;
    }
  }

  // ukloni markere koji nisu u odgovoru (ako endpoint vraća kompletan snapshot)
  for (const [id, marker] of this.vehicleMarkers.entries()) {
    if (!seen.has(id)) {
      this.vehiclesLayer.removeLayer(marker);
      this.vehicleMarkers.delete(id);
    }
  }
}


  private initMap(): void {
    const el = this.mapContainer?.nativeElement;
    if (!el) return;

    // ako je Leaflet već zakačen za ovaj DOM element, resetuj marker
    if ((el as any)._leaflet_id) {
      (el as any)._leaflet_id = null; // <-- ključna stvar za "already initialized"
    }

    if (this.map) return;

    const noviSad: [number, number] = [45.2671, 19.8335];

    // prosledi element, ne string id
    this.map = this.L.map(el, { center: noviSad, zoom: 13 });

    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    this.vehiclesLayer = this.L.layerGroup().addTo(this.map);

    this.invalidateTimer = setTimeout(() => {
      if (this.map) this.map.invalidateSize();
    }, 300);
  }

  private listenToLocations() {
    this.subs.add(
      this.routeDataService.from$.subscribe((from) => {
        this.updateMarker('from', from);
        this.updateRoute();
      })
    );

    this.subs.add(
      this.routeDataService.to$.subscribe((to) => {
        this.updateMarker('to', to);
        this.updateRoute();
      })
    );
    this.subs.add(
      this.routeDataService.estimate$.subscribe((est) => {
        if (!est) {
          this.etaText = '';
          this.distanceKm = null;
          this.durationMin = null;
          this.priceRsd = null;
          this.cdr.detectChanges();
          return;
        }

        this.durationMin = est.durationMin;
        this.distanceKm = Number(est.distanceKm.toFixed(1));
        this.priceRsd = this.calculatePrice(this.distanceKm, this.currentVehicleType);

        this.etaText = `Estimated time: ${this.durationMin} min · Distance: ${this.distanceKm} km · Price: ${this.priceRsd} RSD`;
        this.cdr.detectChanges();
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
        //this.map.removeLayer(this.fromMarker);
        if (this.map && this.map.hasLayer(this.fromMarker)) {
          this.map.removeLayer(this.fromMarker);
        }
        this.fromMarker = null;
      }
      if (which === 'to' && this.toMarker) {
        //this.map.removeLayer(this.toMarker);
        if (this.map && this.map.hasLayer(this.toMarker)) {
          this.map.removeLayer(this.toMarker);
        }
        this.toMarker = null;
      }
      return;
    }

    const marker = this.L.marker([loc.lat, loc.lng], {
      icon: this.redPinIcon(),
    });

    if (which === 'from') {
      //if (this.fromMarker) this.map.removeLayer(this.fromMarker);
      if (this.fromMarker && this.map && this.map.hasLayer(this.fromMarker)) {
        this.map.removeLayer(this.fromMarker);
      }
      this.fromMarker = marker.addTo(this.map).bindPopup('From').openPopup();
    } else {
      //if (this.toMarker) this.map.removeLayer(this.toMarker);
       if (this.toMarker && this.map && this.map.hasLayer(this.toMarker)) {
        this.map.removeLayer(this.toMarker);
      }
      this.toMarker = marker.addTo(this.map).bindPopup('To').openPopup();
    }
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
    const from = this.routeDataService.getFrom();
    const to = this.routeDataService.getTo();

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
      
      this.cdr.detectChanges();
      return;
    }

    const waypoints = [
      this.L.latLng(from.lat, from.lng),
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
      
      if (this.routeDataService.getEstimate()) return; //added if we hace estimate from backend

      const seconds = route.summary.totalTime;
      const meters = route.summary.totalDistance;

      const minutes = Math.round(seconds / 60);
      const km = Number((meters / 1000).toFixed(1));

      this.durationMin = minutes;
      this.distanceKm = km;
      this.priceRsd = this.calculatePrice(km, this.currentVehicleType);

      this.etaText = `Estimated time: ${minutes} min · Distance: ${km} km · Price: ${this.priceRsd} RSD`;
      this.cdr.detectChanges();
    });
  }

  private enableClickToPick1() {
    this.map.on('click', (e: any) => {
      const lat = e.latlng.lat;
      const lng = e.latlng.lng;

      if (this.clickStep === 0 || this.clickStep === 2) {
        this.clickStep = 1;

        this.geocoding.reverse(lat, lng).subscribe((loc) => {
          this.routeDataService.setFrom(loc);
        });

        return;
      }

      if (this.clickStep === 1) {
        this.clickStep = 2;

        this.geocoding.reverse(lat, lng).subscribe((loc) => {
          this.routeDataService.setTo(loc);
        });

        return;
      }
    });
  }

  private enableClickToPick(): void {
    if (!this.map) return;

    this.mapClickHandler = (e: any) => {
      const lat = e.latlng.lat;
      const lng = e.latlng.lng;

      if (this.clickStep === 0 || this.clickStep === 2) {
        this.clickStep = 1;

        this.geocoding.reverse(lat, lng).subscribe((loc) => {
          this.routeDataService.setFrom(loc);
        });

        return;
      }

      if (this.clickStep === 1) {
        this.clickStep = 2;

        this.geocoding.reverse(lat, lng).subscribe((loc) => {
          this.routeDataService.setTo(loc);
        });

        return;
      }
    };

    this.map.on('click', this.mapClickHandler);
  }
}
