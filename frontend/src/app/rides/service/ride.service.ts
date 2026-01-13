import { Injectable, signal } from '@angular/core';
//import { sign } from 'crypto';
import { RideModel } from '../model/ride.model';

@Injectable({
  providedIn: 'root',
})
export class RideService {
  private _rides = signal<RideModel[]>([
    { id: 1, price: 15, dropoff: 'Location A', pickup: 'Location B', statingTime: new Date('2024-07-01T10:00:00'), driver: 'John Doe', isFavouriteRoute: false, status:'ASSIGNED' },
    { id: 2, price: 20, dropoff: 'Location C', pickup: 'Location D', statingTime: new Date('2024-07-02T12:00:00'), driver: 'Jane Smith', isFavouriteRoute: true, status:'ASSIGNED'  },
    { id: 3, price: 25, dropoff: 'Location E', pickup: 'Location F', statingTime: new Date('2026-01-08T15:05:00'), driver: 'Mike Johnson', isFavouriteRoute: false, status:'ASSIGNED'  },
    { id: 4, price: 30, dropoff: 'Location G', pickup: 'Location H', statingTime: new Date('2024-07-04T16:00:00'), driver: 'Emily Davis', isFavouriteRoute: false, status:'ASSIGNED'  },
    { id: 5, price: 35, dropoff: 'Location I', pickup: 'Location J', statingTime: new Date('2024-07-05T18:00:00'), driver: 'David Wilson', isFavouriteRoute: true, status:'ASSIGNED'  },
    { id: 6, price: 40, dropoff: 'Location K', pickup: 'Location L', statingTime: new Date('2026-07-06T20:00:00'), driver: 'Sarah Brown', isFavouriteRoute: false, status:'ASSIGNED'  }
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

  toggleSortOrder() {
    const currentRides = this._rides();
    const isAsc = currentRides[0].statingTime < currentRides[currentRides.length - 1].statingTime;
    const sortedRides = [...currentRides].sort((a, b) =>
      isAsc
        ? b.statingTime.getTime() - a.statingTime.getTime()
        : a.statingTime.getTime() - b.statingTime.getTime()
    );
    this._rides.set(sortedRides);
  }

  getRideById(id: number): RideModel | undefined {
  return this._rides().find(r => r.id === id);
}

// PASSENGER: do 10 min pre starta
canPassengerCancel(rideId: number): boolean {
  const ride = this.getRideById(rideId);
  if (!ride) return false;
  if ((ride.status ?? 'ASSIGNED') !== 'ASSIGNED') return false;

  const startMs = ride.statingTime.getTime();
  const nowMs = Date.now();
  const tenMin = 10 * 60 * 1000;

  return nowMs <= startMs - tenMin;
}

// DRIVER: pre početka (kod tebe nemamo druge statuse pa uzimamo ASSIGNED)
canDriverCancel(rideId: number): boolean {
  const ride = this.getRideById(rideId);
  if (!ride) return false;
  return (ride.status ?? 'ASSIGNED') === 'ASSIGNED';
}

cancelByPassenger(rideId: number): { ok: boolean; message: string } {
  if (!this.canPassengerCancel(rideId)) {
    return { ok: false, message: 'Korisnik može otkazati najkasnije 10 minuta pre početka vožnje.' };
  }

  this._rides.update(rides =>
    rides.map(r =>
      r.id === rideId
        ? { ...r, status: 'CANCELED', canceledBy: 'PASSENGER' }
        : r
    )
  );

  return { ok: true, message: 'Vožnja je otkazana (putnik).' };
}

cancelByDriver(rideId: number, reason: string): { ok: boolean; message: string } {
  if (!this.canDriverCancel(rideId)) {
    return { ok: false, message: 'Vozač može otkazati vožnju samo pre početka.' };
  }

  const clean = (reason ?? '').trim();
  if (clean.length < 5) {
    return { ok: false, message: 'Razlog mora imati minimum 5 karaktera.' };
  }

  this._rides.update(rides =>
    rides.map(r =>
      r.id === rideId
        ? { ...r, status: 'CANCELED', canceledBy: 'DRIVER', cancelReason: clean }
        : r
    )
  );

  return { ok: true, message: 'Vožnja je otkazana (vozač).' };
}

}
