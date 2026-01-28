import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DriverService {
  private apiUrl = 'http://localhost:8080/api/drivers';
  private profileApi = 'http://localhost:8080/api/profile';

  constructor(private http: HttpClient) {}

  createDriver(payload: any): Observable<any> {
    return this.http.post(this.apiUrl, payload);
  }

   /** Dohvatanje profila ulogovanog vozača */
  getMyProfile(): Observable<any> {
    return this.http.get(this.profileApi);
  }

  /** Slanje zahteva adminu za izmenu profila */
  requestProfileChange(payload: any): Observable<void> {
    //return this.http.put<void>(`${this.profileApi}/driver-request`, payload);
    return this.http.put<void>(this.profileApi, payload);

  }
}