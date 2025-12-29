import { Component, Signal } from '@angular/core';
import { DatePipe, CommonModule } from '@angular/common';
import { RideService } from '../service/ride.service';
import { RideModel } from '../model/ride.model';

@Component({
  selector: 'app-driver-ride-history-component',
  imports: [DatePipe, CommonModule],
  templateUrl: './driver.ride.history.component.html',
  styleUrl: './driver.ride.history.component.css',
})
export class DriverRideHistoryComponent {
  protected rides: Signal<RideModel[]>;
  isAscending(): boolean {
    const currentRides = this.rides();
    return currentRides[0].statingTime < currentRides[currentRides.length - 1].statingTime;
  }

  constructor(private service: RideService) {
    this.rides = this.service.rides;
  }

  addRouteToFavourites(event: Event) {
    
    event.stopPropagation();
    
    //ride.isFavouriteRoute = !ride.isFavouriteRoute;
    const target = event.currentTarget as SVGElement;
    const rideId = Number(target.getAttribute('data-ride-id'));
    this.service.toggleFavorite(rideId);
    
  }

  toggleSortOrder() {
    this.service.toggleSortOrder();
  }
}
