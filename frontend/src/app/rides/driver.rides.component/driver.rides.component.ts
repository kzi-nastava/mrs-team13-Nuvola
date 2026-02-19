import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { EndRideService } from '../service/end.ride.service';
import { AuthService } from '../../auth/services/auth.service';
import { RideApiService } from '../service/ride-api.service';
import { ReactiveFormsModule, FormGroup, FormControl, Validators} from '@angular/forms';
import { CancelRideComponent } from '../cancel-ride.component/cancel-ride.component';
import { StopRideComponent } from '../../stop-ride.component/stop-ride.component';


type RideStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'FINISHED' | 'CANCELLED';

type RideLocation = {
  address: string;
};

type DriverRide = {
  id: number;
  scheduledTime: string; 
  from: RideLocation;
  to: RideLocation;
  stops: RideLocation[];
  passengers: string[];

  allPassengersJoined: boolean; 
  status: RideStatus;
  price: number;
  panic?: boolean
  cancelReason?: string;

  stoppedAt?: string;
  stoppedLat?: number;
  stoppedLng?: number;
  stoppedAddress?: string;
};

type StopResult = {
  rideId: number;
  status: string;
  price: number;
  message?: string;
  stoppedAt: string;
  lat: number;
  lng: number;
  address: string;
};

@Component({
  selector: 'app-driver.rides.component',
  imports: [CommonModule, ReactiveFormsModule, CancelRideComponent, StopRideComponent],
  templateUrl: './driver.rides.component.html',
  styleUrl: './driver.rides.component.css',
  standalone: true,
})
export class DriverRidesComponent implements OnInit {
  rides: DriverRide[] = [];
  upcomingRides: DriverRide[] = [];
  activeRide: DriverRide | null = null;
  errorMessage: string | null = null;
  showCancelModal = false;
  cancelRideTarget: DriverRide | null = null;

  showStopModal = false;
  stopRideTarget: DriverRide | null = null;

  cancelForm = new FormGroup({
    reason: new FormControl('', [Validators.required, Validators.minLength(5)]),
  });

  get reason() {
    return this.cancelForm.controls.reason;
  }

  stopResult: StopResult | null = null;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private endRideService: EndRideService,
    private router: Router,  private cdr: ChangeDetectorRef,
    private rideApi: RideApiService 
  ) {}

  

  ngOnInit(): void {
    this.loadRides();
  }



//  get reason() {
  //  return this.cancelForm.controls.reason;
  //}

  loadRides() {
    const username = this.authService.getUsername();
    if (!username) {
      this.errorMessage = 'User not authenticated.';
      return;
    }

    console.log("USERNAME:", username);

    this.http.get<any[]>(`http://localhost:8080/api/drivers/${username}/assigned-rides`)
      .subscribe({
        next: (data) => {
          console.log("=== RAW BACKEND RESPONSE ===", data);
          console.log("=== FIRST RIDE STOPS ===", data[0]?.stops);
          this.rides = data.map(r => this.mapRide(r));
          console.log("=== MAPPED RIDES ===", this.rides);
          console.log("=== FIRST MAPPED RIDE STOPS ===", this.rides[0]?.stops);

          this.upcomingRides = this.rides.filter(r => r.status === 'SCHEDULED');
          this.activeRide = this.rides.find(r => r.status === 'IN_PROGRESS') ?? null;

          this.cdr.detectChanges();

          console.log("UPCOMING:", this.upcomingRides);
          },
            error: () => {
              this.errorMessage = 'Failed to load rides.';
            }
          });
      }

  private mapRide(r: any): DriverRide {
    return {
      id: r.id,
      scheduledTime: r.scheduledTime
        ? new Date(r.scheduledTime).toLocaleString()
        : 'Now',
      from: { address: r.pickup },
      to: { address: r.dropoff },
      stops: r.stops?.map((s: string) => ({ address: s })) ?? [],
      passengers: r.passengers ?? [],
      allPassengersJoined: true,
      status: r.status,
      price: r.price,
      panic: !!r.panic
    };
  }

  // ===== GETTERS =====
get hasActiveRide(): boolean {
  return this.activeRide !== null;
}

  // ===== ACTIONS =====

  startRide(ride: DriverRide) {
    if (!ride.allPassengersJoined) return;

    this.http.put(`http://localhost:8080/api/rides/${ride.id}/start`, {})
      .subscribe({
        next: () => {
          console.log('Ride started successfully');
          this.loadRides();
        },
        error: (err) => {
          console.error('Failed to start ride:', err);
          this.errorMessage = 'Failed to start ride.';
        }
      });
  }

  openCancelModal(ride: DriverRide) {
    console.log('OPEN MODAL FOR', ride.id);
    if (ride.status !== 'SCHEDULED') return;
    this.cancelRideTarget = ride;
    this.cancelForm.reset({ reason: '' });
    this.showCancelModal = true;
  }
  closeCancelModal() {
    this.showCancelModal = false;
    this.cancelRideTarget = null;
    this.cancelForm.reset({ reason: '' });
  }

  stopRide(ride: DriverRide) {
  const ok = confirm('Da li ste sigurni? Vožnja će biti završena na trenutnoj lokaciji.');
  if (!ok) return;

  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      const lat = pos.coords.latitude;
      const lng = pos.coords.longitude;
      const stoppedAt = new Date().toISOString();

      let address = '';
      try {
        address = await this.reverseGeocode(lat, lng);
      } catch {
        address = `${lat}, ${lng}`; 
      }

      const payload = { lat, lng, stoppedAt, address };

      this.rideApi.stopRide(ride.id, payload).subscribe({
        next: (res) => {
          ride.status = res.status as RideStatus;
          ride.price = res.price;

          this.stopResult = {
            rideId: ride.id,
            status: res.status,
            price: res.price,
            message: res.message,
            stoppedAt,
            lat,
            lng,
            address, 
          } as any;

          this.loadRides();
        },
        error: () => this.errorMessage = 'Failed to stop ride.'
      });
    },
    () => this.errorMessage = 'Location permission denied (allow GPS).',
    { enableHighAccuracy: true, timeout: 10000 }
  );
}

  finishRide(ride: DriverRide) {
    const username = this.authService.getUsername();
    if (!username) return;

    this.endRideService.endRide(username).subscribe({
      next: (resp) => {
        const rideId = resp.body;

        if (rideId) {
          this.router.navigate(['/scheduled-ride-start', rideId]);
        }
        this.loadRides();
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Failed to end ride.';
        this.cdr.detectChanges();
      }
    });
  }

  panic(ride: DriverRide) {
  if (ride.panic) return;

  this.http.post(`http://localhost:8080/api/rides/${ride.id}/panic`, {})
    .subscribe({
      next: () => {
        
        ride.panic = true;
        if (this.activeRide?.id === ride.id) {
          this.activeRide.panic = true;
        }
        this.cdr.detectChanges();
        alert('PANIC triggered!');
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to trigger PANIC.';
        alert('PANIC failed!');
      }
    });
}
private async reverseGeocode(lat: number, lng: number): Promise<string> {
  const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`;

  const res = await fetch(url, {
    headers: {
      'Accept': 'application/json',
  
      'User-Agent': 'NuvolaApp/1.0 (local dev)'
    }
  });

  const data = await res.json();
  return data?.display_name ?? `${lat}, ${lng}`;
}


openStopModal(ride: DriverRide) {
  this.stopRideTarget = ride;
  this.showStopModal = true;
}

closeStopModal() {
  this.showStopModal = false;
  this.stopRideTarget = null;
}

onStopConfirmed(_event: any) {
  
  if (!this.stopRideTarget) return;

  this.stopRide(this.stopRideTarget);

  this.closeStopModal();
}


confirmCancel() {
  if (!this.cancelRideTarget) return;

  if (this.cancelForm.invalid) {
    this.cancelForm.markAllAsTouched();
    return;
  }

  const reason = (this.reason.value ?? '').trim();

  this.http.put<any>(
    `http://localhost:8080/api/rides/${this.cancelRideTarget.id}/cancel/driver`,
    { reason }
  ).subscribe({
    next: () => {
      this.closeCancelModal();
      this.loadRides(); // refresh UI
    },
    error: (err) => {
      console.error(err);
      this.errorMessage = err?.error?.message ?? 'Failed to cancel ride.';
    }
  });
}


}
