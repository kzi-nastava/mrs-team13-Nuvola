// import { CommonModule, isPlatformBrowser } from '@angular/common';
// import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
// import { ActivatedRoute } from '@angular/router';
// import {
//   AfterViewInit,
//   ChangeDetectorRef,
//   Component,
//   Inject,
//   OnDestroy,
//   PLATFORM_ID,
// } from '@angular/core';
// import { Observable, Subscription, interval, of } from 'rxjs';
// import { catchError, startWith, switchMap } from 'rxjs/operators';
// import { environment } from '../../env/enviroment';
// import { AuthService } from '../../auth/services/auth.service';
// import { Client } from '@stomp/stompjs';

// interface LocationDTO {
//   latitude: number;
//   longitude: number;
// }

// export interface DriverPositionUpdate {
//   latitude: number;
//   longitude: number;
//   toRemove: boolean;
//   occupied: boolean;
//   driverId: number;
// }

// interface RouteDTO {
//   stops: LocationDTO[];
// }

// interface TrackingRideDTO {
//   id: number;
//   route: RouteDTO;
//   driverId: number;
//   price: number;
//   dropoff: string;
//   pickup: string;
//   startingTime: string;
//   favouriteRoute: boolean;
//   panic: boolean;
// }

// @Component({
//   selector: 'app-admin.driver.info.component',
//   imports: [CommonModule],
//   templateUrl: './admin.driver.info.component.html',
//   styleUrl: './admin.driver.info.component.css',
// })
// export class AdminDriverInfoComponent implements AfterViewInit, OnDestroy {
//   private map: any;
//   private L!: typeof import('leaflet');
//   private Routing: any;

//   private routeControl?: any;
//   private vehicleMarker?: any;
//   private carIcon?: any;

//   private stompClient?: Client;
//   private wsSub?: any;

//   private driverId?: number;
//   private stops: LocationDTO[] = [];

//   // UI state
//   ride: TrackingRideDTO | null = null;
//   rideInProgress = false;
//   infoMessage: string | null = null;
//   errorMessage: string | null = null;

//   // remaining distance/time (radi samo kad ima stops + router)
//   remainingKm: number | null = null;
//   remainingMin: number | null = null;
//   private lastRemainingCalcAt = 0;
//   private remainingRouter?: any;

//   // ako nema ride-a, možeš da "povučeš" poziciju povremeno REST-om
//   private posPollSub?: Subscription;

//   constructor(
//     private http: HttpClient,
//     private authService: AuthService,
//     private cdr: ChangeDetectorRef,
//     private route: ActivatedRoute,
//     @Inject(PLATFORM_ID) private platformId: object
//   ) {}

//   ngOnDestroy(): void {
//     try {
//       this.wsSub?.unsubscribe();
//     } catch {}

//     if (this.stompClient) {
//       this.stompClient.deactivate();
//       this.stompClient = undefined;
//     }

//     this.posPollSub?.unsubscribe();
//   }

//   // ---------------- WS ----------------

//   async initializeWebSocketConnection(driverId: number) {
//     if (!isPlatformBrowser(this.platformId)) return;
//     if (this.stompClient?.active) return;

//     (globalThis as any).global ??= globalThis;

//     const sockjsMod: any = await import('sockjs-client');
//     const SockJS = sockjsMod.default ?? sockjsMod;

//     this.stompClient = new Client({
//       webSocketFactory: () => new SockJS(environment.apiHost + '/ws'),
//       reconnectDelay: 3000,
//       heartbeatIncoming: 10000,
//       heartbeatOutgoing: 10000,
//       debug: () => {},
//       onConnect: () => {
//         this.wsSub = this.stompClient!.subscribe(
//           `/topic/position/${driverId}`,
//           (msg) => this.handlePositionUpdate(JSON.parse(msg.body))
//         );
//       },
//       onStompError: (frame) => console.error('STOMP error', frame),
//     });

//     this.stompClient.activate();
//   }

//   private handlePositionUpdate(update: DriverPositionUpdate) {
//     if (!this.map) return;

//     if (update.toRemove) {
//       if (this.vehicleMarker) {
//         this.map.removeLayer(this.vehicleMarker);
//         this.vehicleMarker = undefined;
//       }
//       this.remainingKm = null;
//       this.remainingMin = null;
//       return;
//     }

//     const pos = { latitude: update.latitude, longitude: update.longitude };
//     this.updateVehicleMarker(pos);
//     this.updateRemainingFromPosition(pos);
//   }

//   // ---------------- MAP ----------------

//   private initMap(center: [number, number] = [45.2396, 19.8227]): void {
//     this.map = this.L.map('ride-tracking-map', { center, zoom: 13 });

//     this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
//       maxZoom: 18,
//       minZoom: 3,
//       attribution:
//         '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
//     }).addTo(this.map);
//   }

//   private buildIcons(): void {
//     const fill = '#e87810';
//     this.carIcon = this.L.divIcon({
//       iconSize: [32, 32],
//       iconAnchor: [16, 16],
//       html: `<div style="
//           width: 26px; height: 26px; border-radius: 999px;
//           background: ${fill};
//           border: 2px solid white;
//           box-shadow: 0 6px 14px rgba(0,0,0,0.20);
//           display:flex; align-items:center; justify-content:center;
//           font-size: 14px;
//         ">🚗</div>`,
//     });
//   }

//   private renderRouteWithInstructions(stops: LocationDTO[]): void {
//     const waypoints = stops.map((s) => this.L.latLng(s.latitude, s.longitude));

//     if (!this.routeControl) {
//       this.routeControl = this.Routing.control({
//         waypoints,
//         addWaypoints: false,
//         draggableWaypoints: false,
//         routeWhileDragging: false,
//         show: true,
//         collapsible: true,
//         fitSelectedRoutes: true,
//       }).addTo(this.map);
//     } else {
//       this.routeControl.setWaypoints(waypoints);
//     }
//   }

//   private clearRoute(): void {
//     if (this.routeControl && this.map) {
//       try {
//         this.map.removeControl(this.routeControl);
//       } catch {}
//       this.routeControl = undefined;
//     }
//   }

//   private updateVehicleMarker(pos: LocationDTO): void {
//     const latlng: [number, number] = [pos.latitude, pos.longitude];
//     if (!this.map) return;

//     if (!this.vehicleMarker) {
//       this.vehicleMarker = this.L.marker(latlng, { icon: this.carIcon })
//         .addTo(this.map)
//         .bindPopup('Vehicle');
//       this.map.setView(latlng, 14);
//     } else {
//       this.vehicleMarker.setLatLng(latlng);
//     }
//   }

//   // ---------------- ETA (remaining) ----------------

//   private ensureRemainingRouter() {
//     if (this.remainingRouter) return;
//     this.remainingRouter = this.Routing.osrmv1({
//       serviceUrl: 'https://router.project-osrm.org/route/v1',
//     });
//   }

//   private updateRemainingFromPosition(pos: LocationDTO) {
//     if (!this.stops?.length) return;

//     const now = Date.now();
//     if (now - this.lastRemainingCalcAt < 2000) return;
//     this.lastRemainingCalcAt = now;

//     this.ensureRemainingRouter();

//     const lastStop = this.stops[this.stops.length - 1];
//     const waypoints = [
//       this.L.Routing.waypoint(this.L.latLng(pos.latitude, pos.longitude)),
//       this.L.Routing.waypoint(this.L.latLng(lastStop.latitude, lastStop.longitude)),
//     ];

//     this.remainingRouter.route(waypoints, (err: any, routes: any[]) => {
//       if (err || !routes?.length) return;
//       const summary = routes[0].summary;
//       this.remainingKm = Math.max(0, summary.totalDistance / 1000);
//       this.remainingMin = Math.max(0, Math.round(summary.totalTime / 60));
//       this.cdr.detectChanges();
//     });
//   }

//   // ---------------- REST ----------------

//   private fetchCurrentRideResponse(): Observable<HttpResponse<TrackingRideDTO>> {
//     if (!this.driverId) {
//       // simuliraj 401-ish ponašanje
//       return of(new HttpResponse<TrackingRideDTO>({ status: 401 }));
//     }

//     // BITNO: observe:'response' da uhvatiš 204
//     return this.http.get<TrackingRideDTO>(
//       `${environment.apiHost}/api/rides/now/user/${this.driverId}`,
//       { observe: 'response' }
//     );
//   }

//   private fetchVehiclePosition(): Observable<LocationDTO> {
//     const username: string = this.authService.getUsername() || '';
//     return this.http.get<LocationDTO>(
//       `${environment.apiHost}/api/rides/now/user/${this.driverId}/position`
//     );
//   }

//   private startPositionPollingIfNoWs() {
//     // polling samo ako želiš (npr. kada nema ride-a)
//     this.posPollSub?.unsubscribe();
//     this.posPollSub = interval(3000)
//       .pipe(startWith(0), switchMap(() => this.fetchVehiclePosition()))
//       .subscribe({
//         next: (pos) => {
//           this.updateVehicleMarker(pos);
//           this.cdr.detectChanges();
//         },
//         error: () => {},
//       });
//   }

//   // ---------------- LIFECYCLE ----------------

//   async ngAfterViewInit(): Promise<void> {
//     if (!isPlatformBrowser(this.platformId)) return;

//     this.L = await import('leaflet');
//     (window as any).L = this.L;

//     (this.L.Icon.Default as any).mergeOptions({
//       iconRetinaUrl: '../images/leaflet/marker-icon-2x.png',
//       iconUrl: '../images/leaflet/marker-icon.png',
//       shadowUrl: '../images/leaflet/marker-shadow.png',
//     });

//     const routingModule: any = await import('leaflet-routing-machine');
//     this.Routing = routingModule.default?.Routing ?? routingModule.Routing;
//     if (!this.Routing) this.Routing = (this.L as any).Routing;

//     this.buildIcons();
//     this.initMap(); // prvo inicijalno, pa posle setView

//     const idParam = this.route.snapshot.paramMap.get('driverId');
//     const parsed = Number(idParam);

//     this.driverId = Number.isFinite(parsed) ? parsed : undefined;

//     if (!this.driverId) {
//       this.errorMessage = 'Missing or invalid driverId in route.';
//       this.cdr.detectChanges();
//       return;
//     }

//     this.fetchCurrentRideResponse()
//       .pipe(
//         catchError((err: HttpErrorResponse) => {
//           // npr. 500, 404...
//           this.errorMessage =
//             err.status === 0
//               ? 'Cannot connect to server (network/CORS).'
//               : 'Failed to load current ride.';
//           this.rideInProgress = false;
//           this.ride = null;
//           this.clearRoute();
//           this.remainingKm = null;
//           this.remainingMin = null;
//           this.cdr.detectChanges();
//           return of(new HttpResponse<TrackingRideDTO>({ status: err.status }));
//         })
//       )
//       .subscribe({
//         next: async (resp) => {
//           // not logged in
//           if (resp.status === 401 || resp.status === 403) {
//             this.errorMessage = 'You must be logged in to track the ride.';
//             this.rideInProgress = false;
//             this.ride = null;
//             this.clearRoute();
//             this.cdr.detectChanges();
//             return;
//           }

//           // 204 -> Ride is not in progress. (nema rute)
//           if (resp.status === 204 || !resp.body) {
//             this.rideInProgress = false;
//             this.ride = null;
//             this.infoMessage = 'Ride is not in progress.';
//             this.clearRoute();
//             this.remainingKm = null;
//             this.remainingMin = null;

//             // ipak povuci trenutnu poziciju i prikaži marker
//             this.fetchVehiclePosition().subscribe({
//               next: (pos) => {
//                 this.updateVehicleMarker(pos);
//                 this.cdr.detectChanges();
//               },
//               error: () => {
//                 // ako i position ne postoji, samo ostaje prazna mapa + poruka
//                 this.cdr.detectChanges();
//               },
//             });

//             // opciono: polling da se marker pomera i bez WS
//             this.startPositionPollingIfNoWs();

//             return;
//           }

//           // 200 -> ima ride
//           const ride = resp.body;
//           this.ride = ride;
//           this.rideInProgress = true;
//           this.infoMessage = null;
//           this.errorMessage = null;

//           this.driverId = ride.driverId;

//           const stops = ride?.route?.stops ?? [];
//           this.stops = stops;

//           if (stops.length >= 2) {
//             // centriraj na prvu tačku
//             this.map.setView([stops[0].latitude, stops[0].longitude], 13);
//             this.renderRouteWithInstructions(stops);

//             // init marker na start (da se vidi odmah)
//             this.updateVehicleMarker(stops[0]);

//             // ETA based on first stop (dok ne stigne WS)
//             this.updateRemainingFromPosition(stops[0]);
//           } else {
//             // nema rute, ali ima ride -> bar marker preko /position
//             this.clearRoute();
//             this.remainingKm = null;
//             this.remainingMin = null;

//             this.fetchVehiclePosition().subscribe({
//               next: (pos) => {
//                 this.updateVehicleMarker(pos);
//                 this.cdr.detectChanges();
//               },
//               error: () => {},
//             });
//           }

//           // WS za live poziciju (ako želiš)
//           await this.initializeWebSocketConnection(this.driverId);
//           this.cdr.detectChanges();
//         },
//       });
//   }
// }
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  Inject,
  OnDestroy,
  PLATFORM_ID,
} from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../env/enviroment';
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
  panic: boolean;
}

@Component({
  selector: 'app-admin.driver.info.component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin.driver.info.component.html',
  styleUrl: './admin.driver.info.component.css',
})
export class AdminDriverInfoComponent implements AfterViewInit, OnDestroy {
  private map: any;
  private L!: typeof import('leaflet');
  private Routing: any;

  private routeControl?: any;
  private vehicleMarker?: any;
  private carIcon?: any;

  private stompClient?: Client;
  private wsSub?: any;

  private driverId?: number;
  private stops: LocationDTO[] = [];

  ride: TrackingRideDTO | null = null;
  rideInProgress = false;
  infoMessage: string | null = null;
  errorMessage: string | null = null;

  remainingKm: number | null = null;
  remainingMin: number | null = null;
  private lastRemainingCalcAt = 0;
  private remainingRouter?: any;

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,
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

  // ---------------- WS ----------------

  async initializeWebSocketConnection(driverId: number) {
    if (!isPlatformBrowser(this.platformId)) return;
    if (this.stompClient?.active) return;

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
          `/topic/position/${driverId}`,
          (msg) => this.handlePositionUpdate(JSON.parse(msg.body))
        );
      },
      onStompError: (frame) => console.error('STOMP error', frame),
    });

    this.stompClient.activate();
  }

  private handlePositionUpdate(update: DriverPositionUpdate) {
    if (!this.map) return;

    // ako backend šalje "toRemove" kad driver offline / nema share
    if (update.toRemove) {
      if (this.vehicleMarker) {
        this.map.removeLayer(this.vehicleMarker);
        this.vehicleMarker = undefined;
      }
      this.remainingKm = null;
      this.remainingMin = null;
      this.cdr.detectChanges();
      return;
    }

    const pos = { latitude: update.latitude, longitude: update.longitude };
    this.updateVehicleMarker(pos);
    this.updateRemainingFromPosition(pos);
  }

  // ---------------- MAP ----------------

  private initMap(center: [number, number] = [45.2396, 19.8227]): void {
    this.map = this.L.map('ride-tracking-map', { center, zoom: 13 });

    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      minZoom: 3,
      attribution:
        '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(this.map);
  }

  private buildIcons(): void {
    const fill = '#7510e8';
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

  private renderRouteWithInstructions(stops: LocationDTO[]): void {
    const waypoints = stops.map((s) => this.L.latLng(s.latitude, s.longitude));

    if (!this.routeControl) {
      this.routeControl = this.Routing.control({
        waypoints,
        addWaypoints: false,
        draggableWaypoints: false,
        routeWhileDragging: false,
        show: true,
        collapsible: true,
        fitSelectedRoutes: true,
      }).addTo(this.map);
    } else {
      this.routeControl.setWaypoints(waypoints);
    }
  }

  private clearRoute(): void {
    if (this.routeControl && this.map) {
      try {
        this.map.removeControl(this.routeControl);
      } catch {}
      this.routeControl = undefined;
    }
  }

  private updateVehicleMarker(pos: LocationDTO): void {
    const latlng: [number, number] = [pos.latitude, pos.longitude];
    if (!this.map) return;

    if (!this.vehicleMarker) {
      this.vehicleMarker = this.L.marker(latlng, { icon: this.carIcon })
        .addTo(this.map)
        .bindPopup('Vehicle');
      this.map.setView(latlng, 14);
    } else {
      this.vehicleMarker.setLatLng(latlng);
    }
  }

  // ---------------- ETA (remaining) ----------------

  private ensureRemainingRouter() {
    if (this.remainingRouter) return;
    this.remainingRouter = this.Routing.osrmv1({
      serviceUrl: 'https://router.project-osrm.org/route/v1',
    });
  }

  private updateRemainingFromPosition(pos: LocationDTO) {
    if (!this.stops?.length) return;

    const now = Date.now();
    if (now - this.lastRemainingCalcAt < 2000) return;
    this.lastRemainingCalcAt = now;

    this.ensureRemainingRouter();

    const lastStop = this.stops[this.stops.length - 1];
    const waypoints = [
      this.L.Routing.waypoint(this.L.latLng(pos.latitude, pos.longitude)),
      this.L.Routing.waypoint(this.L.latLng(lastStop.latitude, lastStop.longitude)),
    ];

    this.remainingRouter.route(waypoints, (err: any, routes: any[]) => {
      if (err || !routes?.length) return;
      const summary = routes[0].summary;
      this.remainingKm = Math.max(0, summary.totalDistance / 1000);
      this.remainingMin = Math.max(0, Math.round(summary.totalTime / 60));
      this.cdr.detectChanges();
    });
  }

  // ---------------- REST (samo ride info, bez pozicije) ----------------

  private fetchCurrentRideResponse(): Observable<HttpResponse<TrackingRideDTO>> {
    if (!this.driverId) return of(new HttpResponse<TrackingRideDTO>({ status: 400 }));

    // TODO: prilagodi putanju tvom backendu:
    // npr: /api/rides/now/driver/{driverId}
    return this.http.get<TrackingRideDTO>(
      `${environment.apiHost}/api/admin/drivers/info/${this.driverId}`,
      { observe: 'response' }
    );
  }

  // ---------------- LIFECYCLE ----------------

  async ngAfterViewInit(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) return;

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

    this.buildIcons();
    this.initMap();

    // driverId iz rute
    const idParam = this.route.snapshot.paramMap.get('driverId');
    const parsed = Number(idParam);
    this.driverId = Number.isFinite(parsed) ? parsed : undefined;

    if (!this.driverId) {
      this.errorMessage = 'Missing or invalid driverId in route.';
      this.cdr.detectChanges();
      return;
    }

    // 1) konektuj WS odmah (da dobiješ poziciju kad stigne)
    await this.initializeWebSocketConnection(this.driverId);

    // 2) učitaj ride (za rutu + startingTime/panic). Bez REST pozicije.
    this.fetchCurrentRideResponse()
      .pipe(
        catchError((err: HttpErrorResponse) => {
          this.errorMessage =
            err.status === 0
              ? 'Cannot connect to server (network/CORS).'
              : 'Failed to load current ride.';
          this.rideInProgress = false;
          this.ride = null;
          this.clearRoute();
          this.remainingKm = null;
          this.remainingMin = null;
          this.cdr.detectChanges();
          return of(new HttpResponse<TrackingRideDTO>({ status: err.status }));
        })
      )
      .subscribe({
        next: (resp) => {
          if (resp.status === 401 || resp.status === 403) {
            this.errorMessage = 'You do not have permission to track this driver.';
            this.rideInProgress = false;
            this.ride = null;
            this.clearRoute();
            this.cdr.detectChanges();
            return;
          }

          // 204 -> nema ride-a: nema rute, nema REST pozicije (WS eventualno ažurira marker)
          if (resp.status === 204 || !resp.body) {
            this.rideInProgress = false;
            this.ride = null;
            this.infoMessage = 'Ride is not in progress.';
            this.clearRoute();
            this.remainingKm = null;
            this.remainingMin = null;
            this.cdr.detectChanges();
            return;
          }

          // 200 -> ima ride: rutu crtamo iz stops
          const ride = resp.body;
          this.ride = ride;
          this.rideInProgress = true;
          this.infoMessage = null;
          this.errorMessage = null;

          const stops = ride?.route?.stops ?? [];
          this.stops = stops;

          if (stops.length >= 2) {
            this.map.setView([stops[0].latitude, stops[0].longitude], 13);
            this.renderRouteWithInstructions(stops);
          } else {
            this.clearRoute();
            this.remainingKm = null;
            this.remainingMin = null;
          }

          this.cdr.detectChanges();
        },
      });
  }
}