import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VehicleType, VehicleTypePricingDTO, UpdateVehicleTypePriceDTO } from '../model/pricing.model';
import { environment } from '../../env/enviroment';

@Injectable({ providedIn: 'root' })
export class PricingService {
  private base = environment.apiHost + '/api/pricing';

  constructor(private http: HttpClient) {}

  // GET /api/pricing/vehicle-types
  getAllVehicleTypePrices(): Observable<VehicleTypePricingDTO[]> {
    return this.http.get<VehicleTypePricingDTO[]>(`${this.base}/vehicle-types`);
  }

  // PUT /api/pricing/vehicle-types/{type}
  upsertVehicleTypePrice(type: VehicleType, basePrice: string): Observable<VehicleTypePricingDTO> {
    const body: UpdateVehicleTypePriceDTO = { basePrice };
    return this.http.put<VehicleTypePricingDTO>(`${this.base}/vehicle-types/${type}`, body);
  }
}