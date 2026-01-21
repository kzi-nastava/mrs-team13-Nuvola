import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

type TabType = 'customers' | 'drivers';

@Component({
  selector: 'app-users',
  imports: [CommonModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css',
})
export class UsersComponent {
 activeTab: TabType = 'customers';

  constructor(private router: Router) {}

  setTab(tab: TabType) {
    this.activeTab = tab;
  }

  isCustomersTab(): boolean {
    return this.activeTab === 'customers';
  }

  isDriversTab(): boolean {
    return this.activeTab === 'drivers';
  }

  onRegisterNewDriver() {
    this.router.navigate(['/register-driver']);
  }
}
