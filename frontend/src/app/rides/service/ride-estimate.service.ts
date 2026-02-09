import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RideEstimateRequestDTO, RideEstimateResponseDTO } from '../model/ride-estimate.model';

@Injectable({ providedIn: 'root' })
export class RideEstimateService {

  private readonly baseUrl = 'http://localhost:8080'; 

  constructor(private http: HttpClient) {}

  estimateRide(payload: RideEstimateRequestDTO): Observable<RideEstimateResponseDTO> {
    return this.http.post<RideEstimateResponseDTO>(
      `${this.baseUrl}/api/rides/estimate`,
      payload
    );
  }
}
