import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { EndRideService, ScheduledRide } from '../service/end.ride.service';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../auth/services/auth.service';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

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

  constructor(private route: ActivatedRoute,
     private endRideService: EndRideService,
      private http: HttpClient,
      private router: Router,
      private cdr: ChangeDetectorRef, 
      private authService: AuthService) {}

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

  startRide(rideId: number): void {
    this.http.put(`http://localhost:8080/api/rides/${rideId}/start`, {})
      .subscribe({
        next: () => {
          console.log('Ride started successfully'); 
          alert('Ride started successfully');
          this.router.navigate(['/driver-rides', this.authService.getUsername()]);
        },
        error: (err) => {
          console.error('Failed to start ride:', err);
          this.errorMessage = 'Failed to start ride.';
          this.cdr.detectChanges();
        }
      });
  }
  
}
