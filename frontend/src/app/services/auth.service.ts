import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private api = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  activateAccount(token: string, password: string): Observable<any> {
    return this.http.post(
      `${this.api}/activate`,
      null,
      {
        params: { token, password }
      }
    );
  }
}
