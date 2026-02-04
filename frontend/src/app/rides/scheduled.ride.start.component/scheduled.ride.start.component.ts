import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { EndRideService, ScheduledRide } from '../service/end.ride.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-scheduled-ride-start-component',
  imports: [DatePipe, DecimalPipe, CommonModule],
  templateUrl: './scheduled.ride.start.component.html',
  styleUrl: './scheduled.ride.start.component.css',
})
export class ScheduledRideStartComponent implements OnInit {
  // nearestRide = {
  //   id: 1,
  //   pickup: '123 Main St',
  //   dropoff: '456 Elm St',
  //   startingTime: '2024-07-01T10:00:00',
  //   driver: 'John Doe',
  //   price: 15.50
  // }

  nearestRide: ScheduledRide | null = null;
  loading: boolean = false;

  errorMessage: string | null = null;

  constructor(private route: ActivatedRoute, private endRideService: EndRideService) {}

  ngOnInit(): void {
    const rideIdParam = this.route.snapshot.paramMap.get('rideId');
    const rideId = Number(rideIdParam);

    if (!rideId || Number.isNaN(rideId)) {
      this.errorMessage = 'Invalid ride id in URL.';
      return;
    }

    this.loading = true;
    this.endRideService.getScheduledRide(rideId).subscribe({
      next: (ride) => {
        this.nearestRide = ride;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load scheduled ride.';
        this.loading = false;
      },
    });
  }
  
}
