import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { Console } from 'console';

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './nav-bar.component.html',
  styleUrls: ['./nav-bar.component.css']
})
export class NavBarComponent {
  menuOpen: boolean = false;

  constructor(private router: Router, public authService: AuthService) {
    
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
    console.log('menuOpen:', this.menuOpen);
  }

  onInboxClick(): void {
    console.log('Inbox clicked');
  }

  onRideHistory(): void {
    this.router.navigate(['/ride-history/', this.authService.username()]);
    this.menuOpen = false;
  }

  onAccount(): void {
    this.router.navigate(['/account-settings/', this.authService.username()]);
    this.menuOpen = false;
  }

  onLogout(): void {
    
    this.authService.logout();
    this.menuOpen = false;
    this.router.navigate(['/login']);
  }

  onLogin(): void {
    this.router.navigate(['/login']);
    this.menuOpen = false;
  }

  onRegister(): void {
    this.router.navigate(['/register']);
    this.menuOpen = false;
  }
}
