import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {environment} from "../../env/enviroment";

export interface RouteEstimateRequest {
  startAddress: string;
  endAddress: string;
}

export interface CoordinateDTO {
  latitude: number;
  longitude: number;
}

export interface RouteEstimateResponse {
  distanceKm: number;
  durationMin: number;

  // Ako backend šalje i geometriju (opciono)
  geometry?: CoordinateDTO[];
}

@Injectable({
  providedIn: 'root'
})
export class RouteEstimateService {
  private readonly baseUrl = `${environment.apiHost}/api/routes/estimate`;


  constructor(private http: HttpClient) {}

  estimateRoute(req: RouteEstimateRequest): Observable<RouteEstimateResponse> {
    return this.http.post<RouteEstimateResponse>(this.baseUrl, req);
  }
}
