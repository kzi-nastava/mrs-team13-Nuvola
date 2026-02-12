import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RegisteredUsersComponent } from '../registered.users.component/registered.users.component';
import { DriversComponent } from '../drivers.component/drivers.component';

type TabType = 'customers' | 'drivers';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, RegisteredUsersComponent, DriversComponent],
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
