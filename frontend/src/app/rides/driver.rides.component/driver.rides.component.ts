import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { StopRideComponent, StopRideResult } from '../stop-ride.component/stop-ride.component';
import { CancelRideComponent } from '../cancel-ride.component/cancel-ride.component';




type RideStatus = 'UPCOMING' | 'IN_PROGRESS' | 'FINISHED' | 'CANCELLED';

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
  cancelReason?: string;

  stoppedAt?: string;
  stoppedLat?: number;
  stoppedLng?: number;
  stoppedAddress?: string;

};

@Component({
  selector: 'app-driver.rides.component',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CancelRideComponent, StopRideComponent],
  templateUrl: './driver.rides.component.html',
  styleUrl: './driver.rides.component.css',
})
export class DriverRidesComponent {
 rides: DriverRide[] = [
    {
      id: 1,
      scheduledTime: 'Today 14:30',
      from: { address: 'Bulevar Oslobođenja 30, Novi Sad' },
      to: { address: 'Železnička 40, Novi Sad' },
      stops: [{ address: 'Kisačka 12, Novi Sad' }],
      passengers: ['elena@gmail.com', 'nadja@gmail.com'],
      allPassengersJoined: false,
      status: 'UPCOMING',
      price: this.getRandomPrice(),
    },
    {
      id: 2,
      scheduledTime: 'Today 16:00',
      from: { address: 'Jevrejska 18, Novi Sad' },
      to: { address: 'Futoška 109, Novi Sad' },
      stops: [],
      passengers: ['marko@gmail.com'],
      allPassengersJoined: true,
      status: 'UPCOMING',
      price: this.getRandomPrice(),
    },
    {
      id: 3,
      scheduledTime: 'Tomorrow 10:15',
      from: { address: 'Cara Dušana 5, Novi Sad' },
      to: { address: 'Bulevar patrijarha Pavla 12, Novi Sad' },
      stops: [
        { address: 'Bulevar cara Lazara 79, Novi Sad' },
        { address: 'Bulevar Evrope 25, Novi Sad' },
      ],
      passengers: ['ana@gmail.com', 'vanja@gmail.com', 'ivana@gmail.com'],
      allPassengersJoined: true,
      status: 'UPCOMING',
      price: this.getRandomPrice(),
    },
  ];

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

  getRandomPrice() {
    return Math.floor(Math.random() * (2000 - 500 + 1)) + 500;
  }

  get activeRide(): DriverRide | null {
    return this.rides.find((r) => r.status === 'IN_PROGRESS') ?? null;
  }

  get hasActiveRide(): boolean {
  return this.rides.some(r => r.status === 'IN_PROGRESS');
}


  get upcomingRides(): DriverRide[] {
    return this.rides.filter((r) => r.status === 'UPCOMING');
  }

  startRide(ride: DriverRide) {
    if (!ride.allPassengersJoined) return;

    this.rides.forEach((r) => {
      if (r.status === 'IN_PROGRESS') r.status = 'UPCOMING';
    });

    ride.status = 'IN_PROGRESS';
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

  cancelRide(ride: DriverRide) {
    if (ride.allPassengersJoined) return;
    ride.status = 'CANCELLED';
  }

  confirmCancel() {
    if (!this.cancelRideTarget) return;

    if (this.cancelForm.invalid) {
      this.cancelForm.markAllAsTouched();
      return;
    }

    const reason = this.reason.value!.trim();
    this.cancelRideTarget.status = 'CANCELLED';
    this.cancelRideTarget.cancelReason = reason;

    this.closeCancelModal();
  }

  stopRide(ride: DriverRide) {
    this.openStopModal(ride);
  }

  finishRide(ride: DriverRide) {
    ride.status = 'FINISHED';
  }

  panic(ride: DriverRide) {
    alert('PANIC triggered! (UI only)');
  }

  openStopModal(ride: DriverRide) {
    // stop samo za active ride
    if (ride.status !== 'IN_PROGRESS') return;
    this.stopRideTarget = ride;
    this.showStopModal = true;
  }
  closeStopModal() {
    this.showStopModal = false;
    this.stopRideTarget = null;
  }

  onStopConfirmed(result: StopRideResult) {
    if (!this.stopRideTarget) return;

    // 1) snimi stop meta
    this.stopRideTarget.stoppedAt = result.stoppedAtIso;
    this.stopRideTarget.stoppedLat = result.lat;
    this.stopRideTarget.stoppedLng = result.lng;
    this.stopRideTarget.stoppedAddress = result.stopAddress;

    // 2) promeni destination
    this.stopRideTarget.to.address = result.stopAddress;

    // 3) nova cena
    this.stopRideTarget.price = result.newPrice;

    // 4) završi vožnju
    this.stopRideTarget.status = 'FINISHED';

    this.closeStopModal();
  }
  
}
