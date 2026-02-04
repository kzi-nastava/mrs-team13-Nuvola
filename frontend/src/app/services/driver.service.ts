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

  getMyProfile(): Observable<any> {
    return this.http.get(this.profileApi);
  }
  requestProfileChange(payload: any): Observable<void> {
    //return this.http.put<void>(`${this.profileApi}/driver-request`, payload);
    return this.http.put<void>(this.profileApi, payload);

  }

  getDriverProfile(): Observable<any> {
  return this.http.get('http://localhost:8080/api/driver/profile');
}


    uploadPicture(formData: FormData) {
  return this.http.post<any>(
    'http://localhost:8080/api/profile/picture',
    formData
  );
}

uploadDriverPicture(driverId: number, formData: FormData) {
  return this.http.post<any>(
    `http://localhost:8080/api/drivers/${driverId}/picture`,
    formData
  );
}


}