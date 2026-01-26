import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export type PanicTriggeredBy = 'DRIVER' | 'PASSENGER';

export interface PanicRequest {
  rideId: number;
  triggeredBy: PanicTriggeredBy;
  time: string; 
  location: { lat: number; lng: number };
}

@Injectable({ providedIn: 'root' })
export class PanicService {
  private http = inject(HttpClient);
  private baseUrl = '/api';


  trigger(rideId: number, payload: PanicRequest) {
    return this.http.put(`${this.baseUrl}/rides/${rideId}/panic`, payload);
  }
}
