import { Injectable, signal } from '@angular/core';
import { sign } from 'crypto';
import { RideModel } from '../model/ride.model';

@Injectable({
  providedIn: 'root',
})
export class RideService {
  private _rides = signal<RideModel[]>([
    { id: 1, price: 15, dropoff: 'Location A', pickup: 'Location B', statingTime: new Date('2024-07-01T10:00:00'), driver: 'John Doe', isFavouriteRoute: false },
    { id: 2, price: 20, dropoff: 'Location C', pickup: 'Location D', statingTime: new Date('2024-07-02T12:00:00'), driver: 'Jane Smith', isFavouriteRoute: true },
    { id: 3, price: 25, dropoff: 'Location E', pickup: 'Location F', statingTime: new Date('2024-07-03T14:00:00'), driver: 'Mike Johnson', isFavouriteRoute: false },
    { id: 4, price: 30, dropoff: 'Location G', pickup: 'Location H', statingTime: new Date('2024-07-04T16:00:00'), driver: 'Emily Davis', isFavouriteRoute: false },
    { id: 5, price: 35, dropoff: 'Location I', pickup: 'Location J', statingTime: new Date('2024-07-05T18:00:00'), driver: 'David Wilson', isFavouriteRoute: true },
    { id: 6, price: 40, dropoff: 'Location K', pickup: 'Location L', statingTime: new Date('2024-07-06T20:00:00'), driver: 'Sarah Brown', isFavouriteRoute: false }
  ]);

  rides = this._rides.asReadonly();

  addRide(ride: RideModel) {
    this._rides.update((rides) => [...rides, ride]);
  }

  toggleFavorite(rideId: number) {
    this._rides.update((rides) =>
      rides.map((ride) =>
        ride.id === rideId
          ? { ...ride, isFavouriteRoute: !ride.isFavouriteRoute }
          : ride
      )
    );
  }
}
