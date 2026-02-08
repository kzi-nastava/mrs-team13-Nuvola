import { Component, Signal, OnInit } from '@angular/core';
import { DatePipe, CommonModule } from '@angular/common';
import { RideService } from '../service/ride.service';
import { GeocodingService } from '../../logedin.homepage/services/geocoding.service';
import { RideModel } from '../model/ride.model';
import { AuthService } from '../../auth/services/auth.service';

interface RideWithAddresses extends RideModel {
  pickupAddress?: string;
  dropoffAddress?: string;
}

@Component({
  selector: 'app-driver-ride-history-component',
  imports: [DatePipe, CommonModule],
  templateUrl: './driver.ride.history.component.html',
  styleUrl: './driver.ride.history.component.css',
})
export class DriverRideHistoryComponent implements OnInit {
  protected rides: Signal<RideModel[]>;
  protected ridesWithAddresses: RideWithAddresses[] = [];
  protected isLoading = false;
  protected error: string | null = null;
  private driverId = 1;

  constructor(
    private service: RideService,
    private geocodingService: GeocodingService,
    private authService: AuthService
  ) {
    this.rides = this.service.rides;
  }

  ngOnInit() {
    this.loadRides();
    console.log('DriverRideHistoryComponent initialized');
    console.log(this.authService.getUsername());
    console.log(this.authService.getRole());
  }

  loadRides(sortOrder?: string) {
    this.isLoading = true;
    this.error = null;

    const currentOrder = sortOrder || (this.isAscending() ? 'asc' : 'desc');

    this.service.loadDriverRides(this.authService.getUsername() || '', 'startingTime', currentOrder)
      .subscribe({
        next: (rides) => {
          this.service.setRides(rides);
          //this.loadAddresses(rides);
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Greška pri učitavanju vožnji:', err);
          this.error = 'Greška pri učitavanju podataka';
          this.isLoading = false;
        }
      });
  }

  // private loadAddresses(rides: RideModel[]) {
  //   // Inicijalizujte sa koordinatama kao fallback
  //   this.ridesWithAddresses = rides.map(ride => ({
  //     ...ride,
  //     pickupAddress: `${ride.pickup.latitude.toFixed(4)}, ${ride.pickup.longitude.toFixed(4)}`,
  //     dropoffAddress: `${ride.dropoff.latitude.toFixed(4)}, ${ride.dropoff.longitude.toFixed(4)}`
  //   }));

  //   // Učitaj adrese asinkrono koristeći vaš geocoding servis
  //   rides.forEach((ride, index) => {
  //     // Reverse geocoding za pickup
  //     this.geocodingService.reverse(
  //       ride.pickup.latitude,
  //       ride.pickup.longitude
  //     ).subscribe({
  //       next: (location) => {
  //         this.ridesWithAddresses[index].pickupAddress = location.address;
  //       },
  //       error: (err) => {
  //         console.error('Geocoding error for pickup:', err);
  //         // Ostaje fallback koordinata
  //       }
  //     });

  //     // Reverse geocoding za dropoff
  //     this.geocodingService.reverse(
  //       ride.dropoff.latitude,
  //       ride.dropoff.longitude
  //     ).subscribe({
  //       next: (location) => {
  //         this.ridesWithAddresses[index].dropoffAddress = location.address;
  //       },
  //       error: (err) => {
  //         console.error('Geocoding error for dropoff:', err);
  //         // Ostaje fallback koordinata
  //       }
  //     });
  //   });
  // }

  isAscending(): boolean {
    const currentRides = this.rides();
    if (currentRides.length < 2) return true;
    return currentRides[0].statingTime < currentRides[currentRides.length - 1].statingTime;
  }

  addRouteToFavourites(event: Event) {
    event.stopPropagation();
    const target = event.currentTarget as SVGElement;
    const rideId = Number(target.getAttribute('data-ride-id'));
    this.service.toggleFavorite(rideId);
  }

  toggleSortOrder() {
    const newOrder = this.isAscending() ? 'desc' : 'asc';
    this.loadRides(newOrder);
  }
}