import { computed, Injectable, signal, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  username = signal<string>('');
  hasNotifications = signal<boolean>(false);

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


  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
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
