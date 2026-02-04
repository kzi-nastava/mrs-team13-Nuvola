import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LocationModel } from '../model/ride.model';
import { environment } from '../../env/enviroment';
import { Observable } from 'rxjs';


export interface ScheduledRide {
  id: number;
  price: number;
  dropoff: LocationModel;
  pickup: LocationModel;
  startingTime: string;
  driver: string;
}


@Injectable({
  providedIn: 'root',
})
export class EndRideService {

  constructor(private http: HttpClient) {}


    endRide(username: string) {
        const url = `${environment.apiHost}/rides/${username}/end`;
        return this.http.put<number>(url, null, { observe: 'response' as const });
    }

    getScheduledRide(rideId: number): Observable<ScheduledRide> {
        const url = `${environment.apiHost}/rides/scheduled-ride/${rideId}`;
        return this.http.get<ScheduledRide>(url);
    }

}