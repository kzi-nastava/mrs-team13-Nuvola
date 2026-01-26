import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface StopRideResponse {
  id: number;
  status: 'FINISHED' | string;
  price: number;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class RideApiService {
  private http = inject(HttpClient);
  private baseUrl = '/api';

  stopRide(rideId: number) {
    return this.http.put<StopRideResponse>(`${this.baseUrl}/rides/${rideId}/stop`, {});
  }
}
