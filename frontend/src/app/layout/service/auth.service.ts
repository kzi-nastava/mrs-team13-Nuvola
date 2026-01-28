import { computed, Injectable, signal, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  username = signal<string>('');
  hasNotifications = signal<boolean>(false);

  role = signal<'ADMIN' | 'REGISTERED_USER' | 'DRIVER'>('REGISTERED_USER');

  private isBrowser: boolean;

  // Computed signal za login status
  isLoggedIn = computed(() => this.username() !== '');

  login(username: string): void {
    this.username.set(username);
    this.hasNotifications.set(true);
    if (this.isBrowser) {
      localStorage.setItem('username', username);
    }
  }

  logout(): void {
    this.username.set('');
    this.hasNotifications.set(false);
    if (this.isBrowser) {
      localStorage.removeItem('username');
    }
  } 


  constructor(@Inject(PLATFORM_ID) private platformId: Object, private http: HttpClient) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    if (this.isBrowser) {
      const savedUsername = localStorage.getItem('username') || '';
      const hasNotif = savedUsername !== '';
      if (savedUsername) {
        this.username.set(savedUsername);
        this.hasNotifications.set(hasNotif);
      }
    }
  }

    private authApi = 'http://localhost:8080/api/auth';
    private profileApi = 'http://localhost:8080/api/profile';

    activateAccount(token: string, password: string): Observable<any> {
      return this.http.post(
        `${this.authApi}/activate`,
        null,
        {
          params: { token, password }
        }
      );
    }
    changePassword(data: {
      currentPassword: string;
      newPassword: string;
    }): Observable<void> {
      return this.http.put<void>(
        `${this.profileApi}/password`,
        data
      );
  }

  // // Metoda za login
  // login(username: string): void {
  //   this.usernameSubject.next(username);
  //   this.notificationsSubject.next(true);
  //   localStorage.setItem('username', username); // Sačuvaj u localStorage
  // }

  // // Metoda za logout
  // logout(): void {
  //   this.usernameSubject.next('');
  //   this.notificationsSubject.next(false);
  //   localStorage.removeItem('username');
  // }

  // // Provera da li je korisnik ulogovan
  // isLoggedIn(): boolean {
  //   return this.usernameSubject.value !== '';
  // }

  // // Dobavljanje trenutnog username-a
  // getCurrentUsername(): string {
  //   return this.usernameSubject.value;
  // }

}
