import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {environment} from "../../env/enviroment";
import { PageResponse, RegisteredUserRideHistoryItemDTO } 
  from '../../history.ride.registereduser.component/model/ride-history.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RideHistoryService {

  private baseUrl = `${environment.apiHost}/api/rides`;

  constructor(private http: HttpClient) {}

  getHistory(opts?: {
    sortBy?: string;
    sortOrder?: 'asc' | 'desc';
    fromDate?: string;
    toDate?: string;
    page?: number;
    size?: number;
  }): Observable<PageResponse<RegisteredUserRideHistoryItemDTO>> {

    let params = new HttpParams();

    if (opts?.sortBy) {
      params = params.set('sortBy', opts.sortBy);
    }

    if (opts?.sortOrder) {
      params = params.set('sortOrder', opts.sortOrder);
    }

    if (opts?.fromDate) {
      params = params.set('fromDate', opts.fromDate);
    }

    if (opts?.toDate) {
      params = params.set('toDate', opts.toDate);
    }

    if (opts?.page != null) {
      params = params.set('page', opts.page.toString());
    }

    if (opts?.size != null) {
      params = params.set('size', opts.size.toString());
    }

    return this.http.get<PageResponse<RegisteredUserRideHistoryItemDTO>>(
      `${this.baseUrl}/history`,
      { params }
    );
  }

  getHistoryDetails(rideId: number): Observable<RegisteredUserRideHistoryItemDTO> {
    return this.http.get<RegisteredUserRideHistoryItemDTO>(
      `${this.baseUrl}/history/${rideId}`
    );
  }
  toggleFavorite(rideId: number) {
  return this.http.put<{ favourite: boolean }>(
    `${this.baseUrl}/${rideId}/favorite`,
    {}
  );
  
}



}