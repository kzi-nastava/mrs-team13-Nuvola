import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/services/auth.service';
import { environment } from '../../env/enviroment';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './nav-bar.component.html',
  styleUrls: ['./nav-bar.component.css']
})
export class NavBarComponent {
  menuOpen: boolean = false;
  hasNotifications: boolean = false;

  constructor(private router: Router, public authService: AuthService, private http: HttpClient, private cdr: ChangeDetectorRef) {
    
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
    console.log('menuOpen:', this.menuOpen);
  }

  onInboxClick(): void {
    const role = this.authService.getRole();
    if (role === 'ROLE_ADMIN') {
      this.router.navigate(['/admin/support/inbox']);
    } else {
      this.router.navigate(['/support/chat']);
    }
    this.menuOpen = false;
  }

  onUsers(): void {
    //this.router.navigate(['/users']);
    this.router.navigate(['/users'], { state: { tab: 'customers' } });
    this.menuOpen = false;
  }
  
  onMyRides(): void {
    this.router.navigate(['/driver-rides/', this.authService.getUsername()]);
    this.menuOpen = false;
  }


  onRideHistory(): void {
    if (this.authService.getRole() === 'ROLE_DRIVER') {
      this.router.navigate(['/ride-history/', this.authService.getUsername()]);
    } else {
      this.router.navigate(['/ride-history']);
    }
    
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
    
    // this.authService.logout();
    // this.menuOpen = false;
    // this.cdr.detectChanges();
    // this.router.navigate(['/login']);
    this.logout();
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

  onTestNotify(): void {
    const url = `${environment.apiHost}/api/profile/notification-test`;
    this.http.put(url, null).subscribe({
      next: () => console.log('Test notification sent successfully'),
      error: (err) => console.error('Error sending test notification', err)
    });
  }

  logout() {
      return this.http.post(environment.apiHost + '/api/auth/logout', {}).subscribe({
      next: () => {
        this.authService.logout();
        this.menuOpen = false;
        this.cdr.detectChanges();
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error('Logout error:', error);
        this.menuOpen = false;
        this.cdr.detectChanges();
      }
    });
}

  onMyNotifications(): void {this.router.navigate(['/notifications']); this.menuOpen = false; }

  onChangePrices(): void {
    this.router.navigate(['/change-prices']);
    this.menuOpen = false;
  }
}
