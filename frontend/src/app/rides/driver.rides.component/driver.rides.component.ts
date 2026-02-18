import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { EndRideService } from '../service/end.ride.service';
import { AuthService } from '../../auth/services/auth.service';


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
};

@Component({
  selector: 'app-driver.rides.component',
  imports: [CommonModule],
  templateUrl: './driver.rides.component.html',
  styleUrl: './driver.rides.component.css',
})
export class DriverRidesComponent implements OnInit {
  rides: DriverRide[] = [];
  upcomingRides: DriverRide[] = [];
  activeRide: DriverRide | null = null;
  errorMessage: string | null = null;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private endRideService: EndRideService,
    private router: Router,  private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadRides();
  }

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

  cancelRide(ride: DriverRide) {
    this.http.put(`http://localhost:8080/api/rides/${ride.id}/cancel`, {
      reason: 'Driver cancelled'
    }).subscribe({
      next: () => this.loadRides(),
      error: () => this.errorMessage = 'Failed to cancel ride.'
    });
  }

  stopRide(ride: DriverRide) {
    this.http.put(`http://localhost:8080/api/rides/${ride.id}/stop`, {})
      .subscribe({
        next: () => this.loadRides(),
        error: () => this.errorMessage = 'Failed to stop ride.'
      });
  }

  finishRide(ride: DriverRide) {
    const username = this.authService.getUsername();
    if (!username) return;

    this.endRideService.endRide(username).subscribe({
      next: (resp) => {
        const rideId = resp.body;
        this.loadRides();
        this.cdr.detectChanges();

        if (rideId) {
          this.router.navigate(['/scheduled-ride-start', rideId]);
        }
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
}
