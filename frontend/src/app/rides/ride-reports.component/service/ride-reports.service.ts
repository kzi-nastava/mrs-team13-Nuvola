import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../env/enviroment';

export interface RideReportDTO {
  date: string;
  rideCount: number;
  totalKm: number;
  totalMoney: number;
}

export interface RideReportResponse {
  data: RideReportDTO[];
  totalRides: number;
  totalKm: number;
  totalMoney: number;
  avgRides: number;
  avgKm: number;
  avgMoney: number;
}

@Injectable({ providedIn: 'root' })
export class RideReportsService {
  constructor(private http: HttpClient) {}

  getMyReport(startDate: string, endDate: string): Observable<RideReportResponse> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<RideReportResponse>(`${environment.apiHost}/api/reports/my`, { params });
  }

  getAdminReport(
    startDate: string,
    endDate: string,
    target: 'ALL_DRIVERS' | 'ALL_CUSTOMERS' | 'ONE_DRIVER' | 'ONE_CUSTOMER',
    email?: string
  ): Observable<RideReportResponse> {
    let params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('target', target);
    if (email) params = params.set('email', email);
    return this.http.get<RideReportResponse>(`${environment.apiHost}/api/reports/admin`, { params });
  }
}