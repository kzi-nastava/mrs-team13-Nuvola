import { CommonModule, isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import {
  AfterViewInit,
  Component,
  Inject,
  OnDestroy,
  PLATFORM_ID,
} from '@angular/core';
import { Observable, Subscription, interval, startWith, switchMap } from 'rxjs';
import { FormsModule } from '@angular/forms';

interface LocationDTO {
  latitude: number;
  longitude: number;
}

interface RouteDTO {
  stops: LocationDTO[];
}

interface TrackingRideDTO {
  id: number;
  route: RouteDTO;
  driverId: number;
  price: number;
  dropoff: string;
  pickup: string;
  startingTime: string;
  favouriteRoute: boolean;
}

interface ReportRequestDTO {
  text: string;
}

@Component({
  selector: 'app-ride-tracking-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './ride.tracking.component.html',
  styleUrl: './ride.tracking.component.css',
})
export class RideTrackingComponent implements AfterViewInit, OnDestroy {
  private map: any;
  private L!: typeof import('leaflet');
  private Routing: any;

  private routeControl?: any;
  private vehicleMarker?: any;
  private carIcon?: any;

  private posSub?: Subscription;

   // ------- REPORT UI STATE -------
  showReportForm = false;
  reportText = '';
  isSending = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  private userId!: number;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  ngOnDestroy(): void {
    this.posSub?.unsubscribe();
  }

  // ----------REPORTING ----------
  toggleReportForm(): void {
    this.showReportForm = !this.showReportForm;
    if (!this.showReportForm) {
      this.reportText = '';
      this.isSending = false;
    }
  }

  submitReport(): void {
    const text = this.reportText.trim();
    if (!text || this.isSending) return;

    this.isSending = true;

    this.sendReport(this.userId, { text }).subscribe({
      next: () => {
        this.isSending = false;
        this.reportText = '';
        this.successMessage = 'Report sent successfully.';
        // this.showReportForm = false;
      },
      error: (err: HttpErrorResponse) => {
        this.isSending = false;

        const backendMsg =
          (typeof err.error === 'string' && err.error) ||
          err.error?.message ||
          err.error?.error ||
          null;

        if (err.status === 0) {
          this.errorMessage = 'Cant connect to server (network/CORS).';
        } else if (err.status === 400) {
          this.errorMessage = backendMsg ?? 'Bad request.';
        } else if (err.status === 401 || err.status === 403) {
          this.errorMessage = 'You do not have permission to send a report.';
        } else {
          this.errorMessage = backendMsg ?? 'Error sending report. Please try again.';
        }
      },
    });
  }

  // rest call to send report
  private sendReport(userId: number, body: ReportRequestDTO): Observable<any> {
    return this.http.post(`http://localhost:8080/api/reports/user/${userId}`, body);
  }



  // ---------- MAP SETUP ----------

  private initMap(center: [number, number] = [45.2396, 19.8227]): void {
    this.map = this.L.map('ride-tracking-map', {
      center,
      zoom: 13,
    });

    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      minZoom: 3,
      attribution:
        '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(this.map);
  }

  private buildIcons(): void {
    const fill = '#e87810';
    this.carIcon = this.L.divIcon({
      iconSize: [32, 32],
      iconAnchor: [16, 16],
      html: `<div style="
          width: 26px; height: 26px; border-radius: 999px;
          background: ${fill};
          border: 2px solid white;
          box-shadow: 0 6px 14px rgba(0,0,0,0.20);
          display:flex; align-items:center; justify-content:center;
          font-size: 14px;
        ">🚗</div>`,
    });
  }

  async ngAfterViewInit(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) return;

    // getting userId from route param
    const idParam = this.route.snapshot.paramMap.get('id');
    const parsed = Number(idParam);
    this.userId = Number.isFinite(parsed) ? parsed : 0;

    if (!this.userId) {
      console.error('Missing or invalid userId param in route.');
      this.initMap();
      return;
    }

    this.L = await import('leaflet');
    (window as any).L = this.L;

    (this.L.Icon.Default as any).mergeOptions({
      iconRetinaUrl: '../images/leaflet/marker-icon-2x.png',
      iconUrl: '../images/leaflet/marker-icon.png',
      shadowUrl: '../images/leaflet/marker-shadow.png',
    });

    const routingModule: any = await import('leaflet-routing-machine');
    this.Routing = routingModule.default?.Routing ?? routingModule.Routing;
    if (!this.Routing) this.Routing = (this.L as any).Routing;

    if (!this.Routing?.control) {
      console.error('Leaflet Routing nije učitan (Routing.control ne postoji).');
      return;
    }

    this.buildIcons();

    // 1) Učitaj trenutnu vožnju i nacrtaj rutu (sa uputstvima)
    this.fetchCurrentRide(this.userId).subscribe({
      next: (ride) => {
        const stops = ride?.route?.stops ?? [];
        if (stops.length < 2) {
          // nema dovoljno tačaka za rutu
          this.initMap();
          return;
        }

        // inicijalni centar = prva stanica
        this.initMap([stops[0].latitude, stops[0].longitude]);

        this.renderRouteWithInstructions(stops);
      },
      error: (e) => {
        console.error(e);
        this.initMap(); // fallback da bar prikaže mapu
      },
    });

    // 2) Polling pozicije vozila (npr. na 2s)
    this.posSub = interval(2000)
      .pipe(
        startWith(0),
        switchMap(() => this.fetchVehiclePosition(this.userId))
      )
      .subscribe({
        next: (pos) => this.updateVehicleMarker(pos),
        error: (e) => console.error(e),
      });
  }

  // ---------- REST ----------
  private fetchCurrentRide(userId: number): Observable<TrackingRideDTO> {
    return this.http.get<TrackingRideDTO>(`http://localhost:8080/api/rides/now/user/${userId}`);
  }

  private fetchVehiclePosition(userId: number): Observable<LocationDTO> {
    return this.http.get<LocationDTO>(`http://localhost:8080/api/rides/now/user/${userId}/position`);
  }

  // ---------- MAP ----------
  private renderRouteWithInstructions(stops: LocationDTO[]): void {
    const waypoints = stops.map((s) => this.L.latLng(s.latitude, s.longitude));

    if (!this.routeControl) {
      this.routeControl = this.Routing.control({
        waypoints,

        // bitno da ti klikovi po mapi ne dodaju nove waypoint-e:
        addWaypoints: false,
        draggableWaypoints: false,
        routeWhileDragging: false,

        // ostavi panel sa uputstvima
        show: true,
        collapsible: true,
        fitSelectedRoutes: true,
      }).addTo(this.map);
    } else {
      this.routeControl.setWaypoints(waypoints);
    }

    // (opciono) reaguj kad se ruta izračuna
    this.routeControl.on('routesfound', (e: any) => {
      const summary = e.routes[0].summary;
      // npr. update UI umesto alert
      console.log(
        `Distance: ${summary.totalDistance / 1000} km, Time: ${Math.round(
          (summary.totalTime % 3600) / 60
        )} min`
      );
    });
  }

  private updateVehicleMarker(pos: LocationDTO): void {
    const latlng: [number, number] = [pos.latitude, pos.longitude];

    if (!this.map) return; // ako vožnja još nije učitana

    if (!this.vehicleMarker) {
      this.vehicleMarker = this.L.marker(latlng, { icon: this.carIcon })
        .addTo(this.map)
        .bindPopup('Vozilo');
    } else {
      this.vehicleMarker.setLatLng(latlng);
    }
  }
}
