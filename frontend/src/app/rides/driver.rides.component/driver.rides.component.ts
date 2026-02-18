import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { EndRideService } from '../service/end.ride.service';
import { AuthService } from '../../auth/services/auth.service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { StopRideComponent, StopRideResult } from '../../stop-ride.component/stop-ride.component';
import { CancelRideComponent } from '../cancel-ride.component/cancel-ride.component';


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

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private endRideService: EndRideService,
    private router: Router,  private cdr: ChangeDetectorRef
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
    if (ride.allPassengersJoined) return;
    this.cancelRideTarget = ride;
    this.cancelForm.reset({ reason: '' });
    this.showCancelModal = true;
  }
  closeCancelModal() {
    this.showCancelModal = false;
    this.cancelRideTarget = null;
    this.cancelForm.reset({ reason: '' });
  }

  //cancelRide(ride: DriverRide) {
    //if (ride.allPassengersJoined) return;
    //ride.status = 'CANCELLED';
  //}

  confirmCancel() {  
      if (!this.cancelRideTarget) return;    
      if (this.cancelForm.invalid) {      
        this.cancelForm.markAllAsTouched();      
        return;    
      }    
      const reason = this.reason.value!.trim();   
      // Send to backend    
      this.http.put(`http://localhost:8080/api/rides/${this.cancelRideTarget.id}/cancel`, { reason })      
      .subscribe({        
        next: () => {          
          if (this.cancelRideTarget) {
            this.cancelRideTarget.status = 'CANCELLED';            
            this.cancelRideTarget.cancelReason = reason;          
          }          
          this.closeCancelModal();          
          this.loadRides();        },        
          error: () => {          
            this.errorMessage = 'Failed to cancel ride.';          
            this.closeCancelModal();        
          }      
        });  
      }

  openStopModal(ride: DriverRide) {
    console.log('OPEN STOP MODAL FOR', ride.id);    
    this.stopRideTarget = ride;    
    this.showStopModal = true;  
  }
  closeStopModal() {
    this.showStopModal = false;    
    this.stopRideTarget = null;  
  }
  stopRide(ride: DriverRide) {    
    this.openStopModal(ride); 
   }

  onStopConfirmed(result: StopRideResult) {
    if (!this.stopRideTarget) return;    
    // Update ride with stop info    
    this.stopRideTarget.stoppedAt = result.stoppedAtIso;    
    this.stopRideTarget.stoppedLat = result.lat;    
    this.stopRideTarget.stoppedLng = result.lng;    
    this.stopRideTarget.stoppedAddress = result.stopAddress;    
    this.stopRideTarget.price = result.newPrice;    
    // Send to backend    
    this.http.put(`http://localhost:8080/api/rides/${this.stopRideTarget.id}/stop`, {      
      stoppedAt: result.stoppedAtIso,      
      lat: result.lat,      
      lng: result.lng,      
      address: result.stopAddress,      
      newPrice: result.newPrice    
    }).subscribe({      
      next: () => {        
        console.log('Ride stopped successfully');        
        this.closeStopModal();        
        this.loadRides();      
      },      
      error: (err) => {        
        console.error('Failed to stop ride:', err);        
        this.errorMessage = 'Failed to stop ride.';        
        this.closeStopModal();      
      }    
    });  
  }


  finishRide(ride: DriverRide) {
    const username = this.authService.getUsername();
    if (!username) return;

    this.endRideService.endRide(username).subscribe({
      next: (resp) => {
        const rideId = resp.body;
        this.loadRides();

        if (rideId) {
          this.router.navigate(['/scheduled-ride-start', rideId]);
        }
      },
      error: () => {
        this.errorMessage = 'Failed to end ride.';
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
