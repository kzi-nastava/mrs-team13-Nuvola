import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-logedin-navbar',
  imports: [CommonModule, RouterModule],
  templateUrl: './logedin.navbar.component.html',
  styleUrl: './logedin.navbar.component.css',
})
export class LogedinNavbarComponent {
  hasNotifications: boolean = true;

  onLogout() {
    console.log('Logging out...');
  }
}
