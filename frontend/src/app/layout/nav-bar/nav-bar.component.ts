import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './nav-bar.component.html',
  styleUrls: ['./nav-bar.component.css']
})
export class NavBarComponent {

  menuOpen = false;

  constructor(private router: Router) {}

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
    console.log('menuOpen:', this.menuOpen);
  }

  onInboxClick(): void {
    console.log('Inbox clicked');
  }

  onRideHistory(): void {
    this.router.navigate(['/ride-history']);
    this.menuOpen = false;
  }

  onAccount(): void {
    this.router.navigate(['/account']);
    this.menuOpen = false;
  }

  onLogout(): void {
    this.router.navigate(['/login']);
    this.menuOpen = false;
  }
}
