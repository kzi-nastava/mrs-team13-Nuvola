import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../env/enviroment';
import { tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class FavoriteApiService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiHost}/api/favorites`; 

  getFavorites() {
    console.log('=== FAVORITES REQUEST to:', this.baseUrl);
    return this.http.get<any[]>(this.baseUrl).pipe(
      tap({
        next: (data) => console.log('=== FAVORITES RESPONSE:', data),
        error: (err) => console.error('=== FAVORITES ERROR:', err.status, err.error)
      })
    );
  }

  removeFavorite(id: number) {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
