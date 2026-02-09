import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
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

  constructor(private route: ActivatedRoute, private endRideService: EndRideService, private cdr: ChangeDetectorRef) {}

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
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = null;
        this.loading = false;
        this.nearestRide = {
          id: 2,
          pickup: {
            latitude: 45.2671,
            longitude: 19.8335
          },
          dropoff: {
            latitude: 45.2550,
            longitude: 19.8450
          },
          startingTime: '2024-07-01T10:00:00',
          driver: 'John Doe',
          price: 15.50
        }
        this.cdr.detectChanges();

      },
    });
  }
  
}
