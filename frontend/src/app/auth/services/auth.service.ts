import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {BehaviorSubject, Observable} from "rxjs";
import {environment} from "../../env/enviroment";
import {JwtHelperService} from '@auth0/angular-jwt';
import { AuthResponse } from '../model/auth.response';
import { LoginModel } from '../model/login.model';
import { RegisterModel } from '../model/register.model';
import { DriverLocationPublisherService } from '../../services/driver.location.publisher.service';
import { NotificationSocketService } from '../../notifications/services/notification.socket.service';
import { SupportChatWsService } from '../../chat/services/support.chat.ws.service';

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

  constructor(private http: HttpClient, 
              private driverLocationPublisher: DriverLocationPublisherService, 
              private notifSocket: NotificationSocketService,
              private chatWs: SupportChatWsService) {
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

  getUserId(): number | null {
    if (!this.isLoggedIn()) return null;

    const token = localStorage.getItem('user');
    if (!token) return null;

    const helper = new JwtHelperService();
    const decoded: any = helper.decodeToken(token);

    const id = decoded.userId;
    if (id === undefined || id === null) return null;

    // in case it's a string, try to convert to number, if it's not a valid number return null
    const num = Number(id);
    return Number.isFinite(num) ? num : null;
  }

  logout(): void {
    localStorage.removeItem('user');
    this.user$.next(this.getRole());
    this.driverLocationPublisher.stop();
    this.notifSocket.disconnect();
    this.chatWs.disconnect();
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
    private authApi = 'http://localhost:8080/api/auth';
    activateAccount(token: string, password: string): Observable<any> {
      return this.http.post(
        `${this.authApi}/activate`,
        null,
        {
          params: { token, password }
        }
      );
    }

  activateEmail(token: string): Observable<string> {
    return this.http.get(
      environment.apiHost + '/api/auth/activate-email',
      {
        params: { token },
        responseType: 'text'
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
      environment.apiHost + '/api/profile/picture',
      formData
    );
  }

  forgotPassword(email: string): Observable<string> {
  return this.http.post(
    environment.apiHost + '/api/auth/forgot-password',
    { email },
    { headers: this.headers, responseType: 'text' }
  );
}

resetPassword(token: string, newPassword: string, confirmNewPassword: string): Observable<string> {
  return this.http.post(
    environment.apiHost + '/api/auth/reset-password',
    { newPassword, confirmNewPassword },
    { headers: this.headers, params: { token }, responseType: 'text' }
  );
}

driverSetPassword(token: string, newPassword: string, confirmNewPassword: string): Observable<string> {
  return this.http.post(
    `http://localhost:8080/api/auth/reset-password?token=${encodeURIComponent(token)}`,
    { newPassword, confirmNewPassword },
    { headers: this.headers, responseType: 'text' }
  );
}

// logout() {
//     this.driverLocationPublisher.stop();
//           return this.http.post(environment.apiHost + '/api/auth/logout', {}).subscribe({
//       next: () => {
//         localStorage.removeItem('user');  
//         this.user$.next(this.getRole());
//         this.driverLocationPublisher.stop();
//         this.notifSocket.disconnect();
//         this.chatWs.disconnect();
//       },
//       error: (error) => {
//         console.error('Logout error:', error);
//       }
//     });
// }
}
