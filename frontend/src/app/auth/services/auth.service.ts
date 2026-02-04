import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {BehaviorSubject, Observable} from "rxjs";
import {environment} from "../../env/enviroment";
import {JwtHelperService} from '@auth0/angular-jwt';
import { AuthResponse } from '../model/auth.response';
import { LoginModel } from '../model/login.model';
import { RegisterModel } from '../model/register.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private headers = new HttpHeaders({
    'Content-Type': 'application/json',
    skip: 'true',
  });

  user$ = new BehaviorSubject("");
  userState = this.user$.asObservable();

  constructor(private http: HttpClient) {
    this.user$.next(this.getRole());
  }

  login(auth: LoginModel): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(environment.apiHost + '/api/auth/login', auth, {
      headers: this.headers,
    });
  }

  getRole(): any {
    if (this.isLoggedIn()) {
      const accessToken: any = localStorage.getItem('user');
      const helper = new JwtHelperService();
      const rolesStr: string | undefined = helper.decodeToken(accessToken).roles;
      if (!rolesStr) return null;
      
      return rolesStr.replace('[', '').replace(']', '').split(',')[0]?.trim() ?? null;
    }
    return null;
  }

  isLoggedIn(): boolean {
    return localStorage.getItem('user') != null;
  }

  getUsername(): string | null {
    if (this.isLoggedIn()) {
      const token = localStorage.getItem('user');
      if (!token) return null;

      const helper = new JwtHelperService();
      const decoded: any = helper.decodeToken(token);

      return decoded.sub ?? null;
    }
    return null;
  }

  logout(): void {
    localStorage.removeItem('user');
    this.user$.next(this.getRole());
  }


  setUser(): void {
    this.user$.next(this.getRole());
  }

  register(registerData: RegisterModel): Observable<any> {
    return this.http.post(
      environment.apiHost + '/api/auth/register',
      registerData,
      {
        headers: this.headers,
      }
    );
  }

  changePassword(data: {
  currentPassword: string;
  newPassword: string;
}): Observable<void> {
  return this.http.put<void>(
    environment.apiHost + '/api/profile/password',
    data
  );
}
uploadProfilePicture(formData: FormData) {
  return this.http.post(
    'http://localhost:8080/api/profile/picture',
    formData
  );
}

}
