import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CreatedRideDTO {
  id: number;
  status: string;   // FINISHED
  price: number;    // recalculated (stub je 900.0 u backendu)
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class RideApiService {
  private http = inject(HttpClient);
  private baseUrl = '/api/rides';

  private apiUrl = 'http://localhost:8080/api/rides';

  createRide(payload: any): Observable<CreatedRideDTO> {
    return this.http.post<CreatedRideDTO>(this.baseUrl, payload);
  }
  
  stopRide(rideId: number): Observable<CreatedRideDTO> {
    return this.http.put<CreatedRideDTO>(`${this.baseUrl}/${rideId}/stop`, {});
  }

}
