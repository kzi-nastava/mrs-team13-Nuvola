import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { LocationModel } from '../models/location.model';

type NominatimSearchResult = {
  display_name: string;
  lat: string;
  lon: string;
  address?: {
    road?: string;
    house_number?: string;
    suburb?: string;
    neighbourhood?: string;
    city?: string;
  };
};

type NominatimReverseResult = {
  display_name: string;
  lat: string;
  lon: string;
  address?: {
    road?: string;
    house_number?: string;
    suburb?: string;
    neighbourhood?: string;
  };
};


@Injectable({ providedIn: 'root' })
export class GeocodingService {
  private baseUrl = 'https://nominatim.openstreetmap.org';

  constructor(private http: HttpClient) {}

search(query: string): Observable<LocationModel[]> {
  const params = new HttpParams()
    .set('q', query)
    .set('format', 'json')
    .set('addressdetails', '1')
    .set('limit', '5');

  return this.http
    .get<NominatimSearchResult[]>(`${this.baseUrl}/search`, { params })
    .pipe(
      map((results) =>
        results.map((r) => {
          const road = r.address?.road ?? '';
          const house = r.address?.house_number ?? '';
          const suburb = r.address?.suburb ?? r.address?.neighbourhood ?? '';

          const short =
            [road, house].filter(Boolean).join(' ') +
            (suburb ? `, ${suburb}` : '');

          return {
            address: short || r.display_name,
            lat: parseFloat(r.lat),
            lng: parseFloat(r.lon),
          };
        })
      )
    );
}

reverse(lat: number, lng: number): Observable<LocationModel> {
  const params = new HttpParams()
    .set('lat', lat)
    .set('lon', lng)
    .set('format', 'json')
    .set('addressdetails', '1');

  return this.http
    .get<NominatimReverseResult>(`${this.baseUrl}/reverse`, { params })
    .pipe(
      map((r) => {
        const road = r.address?.road ?? '';
        const house = r.address?.house_number ?? '';
        const suburb = r.address?.suburb ?? r.address?.neighbourhood ?? '';

        const short =
          [road, house].filter(Boolean).join(' ') +
          (suburb ? `, ${suburb}` : '');

        return {
          address: short || r.display_name,
          lat: parseFloat(r.lat),
          lng: parseFloat(r.lon),
        };
      })
    );
}


}
