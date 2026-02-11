import { CommonModule, isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  Inject,
  OnDestroy,
  PLATFORM_ID,
} from '@angular/core';
import { Observable, Subscription, interval, startWith, switchMap } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { environment } from '../../env/enviroment';
import { AuthService } from '../../auth/services/auth.service';

import { Client } from '@stomp/stompjs';

interface LocationDTO {
  latitude: number;
  longitude: number;
}

export interface DriverPositionUpdate {
  latitude: number;
  longitude: number;
  toRemove: boolean;
  occupied: boolean;
  driverId: number;
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
  reason: string;
  authorUsername: string;
  rideId: number;
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

  private driverId!: number;
  private stompClient?: Client;
  private wsSub?: any;           // StompSubscription
  private useWebSocket = true;


  // ------- WEBSOCKETS -------

  async initializeWebSocketConnection() {
    if (!isPlatformBrowser(this.platformId)) return;
    if (this.stompClient?.active) return;

    // polyfill za biblioteke koje očekuju Node "global"
    (globalThis as any).global ??= globalThis;

    const sockjsMod: any = await import('sockjs-client');
    const SockJS = sockjsMod.default ?? sockjsMod;

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(environment.apiHost + '/ws'),
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => {},
      onConnect: () => {
        this.wsSub = this.stompClient!.subscribe(
          `/topic/position/${this.driverId}`,
          (msg) => this.handlePositionUpdate(JSON.parse(msg.body))
        );
      },
      onStompError: (frame) => console.error('STOMP error', frame),
    });

    this.stompClient.activate();
  }

  // initializeWebSocketConnection() {
  //   if (this.stompClient?.active) return;

  //   this.stompClient = new Client({
  //     webSocketFactory: () => new SockJS(environment.apiHost + '/ws'),
  //     reconnectDelay: 3000,
  //     heartbeatIncoming: 10000,
  //     heartbeatOutgoing: 10000,
  //     debug: () => {},
  //     onConnect: () => {
  //       this.wsSub = this.stompClient!.subscribe(`/topic/position/${this.driverId}`, (msg) => {
  //         const update: DriverPositionUpdate = JSON.parse(msg.body);
  //         this.handlePositionUpdate(update);
  //       });
  //     },
  //     onStompError: (frame) => {
  //       console.error('STOMP error', frame);
  //     },
  //   });

  //   this.stompClient.activate();
  // }


  handlePositionUpdate(update: DriverPositionUpdate) {
    if (!this.map) return;

    if (update.toRemove) {
      if (this.vehicleMarker) {
        this.map.removeLayer(this.vehicleMarker);
        this.vehicleMarker = undefined;
      }
      return;
    }

    this.updateVehicleMarker({ latitude: update.latitude, longitude: update.longitude });
  }

   // ------- REPORT UI STATE -------
  showReportForm = false;
  reportText = '';
  isSending = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  private rideId!: number;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  ngOnDestroy(): void {
    try {
      this.wsSub?.unsubscribe();
    } catch {}

    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = undefined;
    }
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
    

    const username: string = this.authService.getUsername() || '';
    if (username.length === 0) {
      this.isSending = false;
      this.errorMessage = 'You must be logged in to send a report.';
      return;
    }

    this.sendReport({ reason: this.reportText, authorUsername: username, rideId: this.rideId }).subscribe({
      next: () => {
        this.isSending = false;
        this.reportText = '';
        this.successMessage = 'Report sent successfully.';
        this.errorMessage = null;
        this.cdr.detectChanges();
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
        this.cdr.detectChanges();
        
      },
    });
  }

  // rest call to send report
  private sendReport(body: ReportRequestDTO): Observable<any> {
    return this.http.post(environment.apiHost + `/api/rides/report`, body);
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
    const idParam = this.route.snapshot.paramMap.get('rideId');
    const parsed = Number(idParam);
    this.rideId = Number.isFinite(parsed) ? parsed : 0;

    if (!this.rideId) {
      console.error('Missing or invalid rideId param in route.');
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
    this.fetchCurrentRide().subscribe({
      next: async (ride) => {
        const stops = ride?.route?.stops ?? [];
        if (stops.length < 2) {
          // nema dovoljno tačaka za rutu
          this.initMap();
          return;
        }

        if (this.vehicleMarker) {
          this.map.removeLayer(this.vehicleMarker);
          this.vehicleMarker = undefined;
        }

        this.driverId = ride.driverId;

        // inicijalni centar = prva stanica
        this.initMap([stops[0].latitude, stops[0].longitude]);

        this.renderRouteWithInstructions(stops);
        //this.initializeWebSocketConnection();
        await this.initializeWebSocketConnection();
      },
      error: (e) => {
        console.error(e);
        this.initMap(); // fallback da bar prikaže mapu
      },
    });

    
  }

  // ---------- REST ----------
  private fetchCurrentRide(): Observable<TrackingRideDTO> {
    const username: string = this.authService.getUsername() || '';
    if (username.length === 0) {
      this.errorMessage = 'You must be logged in to track the ride.';
      return new Observable<TrackingRideDTO>();
    }
    return this.http.get<TrackingRideDTO>(`http://localhost:8080/api/rides/now/user/${username}`);
  }

  private fetchVehiclePosition(): Observable<LocationDTO> {
    const username: string = this.authService.getUsername() || '';
    if (username.length === 0) {
      this.errorMessage = 'You must be logged in to track the ride.';
      return new Observable<LocationDTO>();
    }
    return this.http.get<LocationDTO>(`http://localhost:8080/api/rides/now/user/${username}/position`);
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

    if (!this.map) return; // if the ride is not yet loaded

    if (!this.vehicleMarker) {
      this.vehicleMarker = this.L.marker(latlng, { icon: this.carIcon })
        .addTo(this.map)
        .bindPopup('Vehicle');
    } else {
      this.vehicleMarker.setLatLng(latlng);
    }
  }
}
