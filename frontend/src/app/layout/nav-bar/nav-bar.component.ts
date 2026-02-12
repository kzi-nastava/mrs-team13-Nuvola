import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/services/auth.service';
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

  onUsers(): void {
    this.router.navigate(['/users']);
    this.menuOpen = false;
  }
  
  onMyRides(): void {
    this.router.navigate(['/driver-rides/', this.authService.getUsername()]);
    this.menuOpen = false;
  }


  onRideHistory(): void {
    this.router.navigate(['/ride-history/', this.authService.getUsername()]);
    this.menuOpen = false;
  }

onAccount(): void {
  if (this.authService.getRole() === 'ROLE_DRIVER') {
    this.router.navigate(['/driver-account']);
  } else {
    this.router.navigate(['/account-settings']);
  }
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

  onLogoClick(): void {
  if (this.authService.isLoggedIn()) {
    this.router.navigate(['/logedin-home/', this.authService.getUsername()]);
  } else {
    this.router.navigate(['/homepage']);
  }

  this.menuOpen = false;
}

  onGrade(): void {
    this.router.navigate(['/grading/', 2]);
    this.menuOpen = false;
  }
}
