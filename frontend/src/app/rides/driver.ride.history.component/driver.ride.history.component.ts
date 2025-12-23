import { Component, Signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RideService } from '../service/ride.service';
import { RideModel } from '../model/ride.model';
import { LogedinNavbarComponent } from '../../layout/logedin.navbar.component/logedin.navbar.component';

@Component({
  selector: 'app-driver-ride-history-component',
  imports: [DatePipe, LogedinNavbarComponent],
  templateUrl: './driver.ride.history.component.html',
  styleUrl: './driver.ride.history.component.css',
})
export class DriverRideHistoryComponent {
  protected rides: Signal<RideModel[]>;

  constructor(private service: RideService) {
    this.rides = this.service.rides;
  }

  addRouteToFavourites(event: Event) {
    
    event.stopPropagation();
    
    // Implement the logic to add the route to favorites
    // fill the icon
    //ride.isFavouriteRoute = !ride.isFavouriteRoute;
    const target = event.currentTarget as SVGElement;
    const rideId = Number(target.getAttribute('data-ride-id'));
    this.service.toggleFavorite(rideId);
    

  }
}
