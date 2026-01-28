import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';


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
};

@Component({
  selector: 'app-driver.rides.component',
  imports: [CommonModule],
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

  cancelRide(ride: DriverRide) {
    if (ride.allPassengersJoined) return;
    ride.status = 'CANCELLED';
  }

  stopRide(ride: DriverRide) {
    alert('Ride paused/stopped (UI only).');
  }

  finishRide(ride: DriverRide) {
    ride.status = 'FINISHED';
  }

  panic(ride: DriverRide) {
    alert('PANIC triggered! (UI only)');
  }
}
