import { Injectable, inject } from '@angular/core';
import { FavoriteRoute } from '../models/favorite-route.model';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class FavoriteApiService {
  private http = inject(HttpClient);
  private baseUrl = '/api/favorites';

  getFavorites() {
    return this.http.get<FavoriteRoute[]>(this.baseUrl);
  }

  removeFavorite(id: number) {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
