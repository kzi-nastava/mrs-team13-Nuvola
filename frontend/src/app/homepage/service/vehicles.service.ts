import { Injectable } from '@angular/core';
import { VehicleLocationDTO } from '../model/vehicle.location';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class VehiclesService {
   constructor(private http: HttpClient) {}

  
  getCurrentPositions(): Observable<VehicleLocationDTO[]> {
    return this.http.get<VehicleLocationDTO[]>('http://localhost:8080/api/drivers/active-vehicles');
  }
}
