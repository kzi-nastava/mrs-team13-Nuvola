import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../env/enviroment';

export interface CreatedRideDTO {
  id: number;
  status: string;   // FINISHED
  price: number;    // recalculated (stub je 900.0 u backendu)
  message?: string;
}

export interface StopRideRequestDTO {
  lat: number;
  lng: number;
  stoppedAt: string; // YYYY-MM-DDTHH:mm:ss
  address?: string;
}

@Injectable({ providedIn: 'root' })
export class RideApiService {
  private http = inject(HttpClient);
private baseUrl = environment.apiHost + '/api/rides';

  private apiUrl = 'http://localhost:8080/api/rides';

  createRide(payload: any): Observable<CreatedRideDTO> {
    return this.http.post<CreatedRideDTO>(this.baseUrl, payload);
  }
  
  stopRide(rideId: number, body: StopRideRequestDTO): Observable<CreatedRideDTO> {
  return this.http.patch<CreatedRideDTO>(`${this.baseUrl}/${rideId}/stop`, body);
  }

  cancelRide(rideId: number, reason?: string | null) {
  return this.http.put<CreatedRideDTO>(`${this.baseUrl}/${rideId}/cancel`, { reason: reason ?? null });
}



  // ride-api.service.ts
reorderRide(payload: { 
  routeId: number; 
  scheduledTime?: string | null; 
}): Observable<CreatedRideDTO> {
  return this.http.post<CreatedRideDTO>(`${this.baseUrl}/reorder`, payload);
}


}
