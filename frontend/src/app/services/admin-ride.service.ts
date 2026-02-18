import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminRideHistory, AdminRideDetails, AdminRideResponse } from '../admin-ride-history.component/model/admin-ride-history.model';

@Injectable({
  providedIn: 'root'
})
export class AdminRideService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  /**
   *
   * @param userId 
   * @param page  
   * @param size 
   * @param sortBy 
   * @param sortDir 
   * @param creationDate 
   */
  getRideHistory(
    userId: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'startTime',
    sortDir: string = 'DESC',
    creationDate?: string
  ): Observable<AdminRideResponse> {
    let params = new HttpParams();
    params = params.set('page', page.toString());
    params = params.set('size', size.toString());
    params = params.set('sortBy', sortBy);
    params = params.set('sortDir', sortDir);

    if (creationDate) {
      params = params.set('creationDate', creationDate);
    }

    return this.http.get<AdminRideResponse>(
      `${this.apiUrl}/users/${userId}/rides`,
      { params }
    );
  }

  /**
   * 
   * @param rideId - ID voznje
   */
  getRideDetails(rideId: number): Observable<AdminRideDetails> {
    return this.http.get<AdminRideDetails>(
      `${this.apiUrl}/rides/${rideId}`
    );
  }
}
