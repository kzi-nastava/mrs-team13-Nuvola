import { Component, OnInit  } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RegisteredUsersComponent } from '../registered.users.component/registered.users.component';
import { DriversComponent } from '../drivers.component/drivers.component';
import { ProfileChangeRequestComponent } from '../profile.change.request.component/profile.change.request.component'; 


type TabType = 'customers' | 'drivers' | 'requests';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, RegisteredUsersComponent, DriversComponent,  ProfileChangeRequestComponent],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css',
})
export class UsersComponent implements OnInit {
  activeTab: TabType = 'customers';

  customersVisible = false;
  driversVisible = false;
  requestsVisible = false;


  constructor(private router: Router) {}

  ngOnInit(): void {
    // Pročitaj koji tab treba otvoriti (prosleđen iz navbara)
    const nav = this.router.getCurrentNavigation();
    const requestedTab = nav?.extras?.state?.['tab'] as TabType ?? 'customers';
    this.showTab(requestedTab);
  }

  showTab(tab: TabType): void {
    this.activeTab = tab;

    if (tab === 'customers') {
      this.customersVisible = true;
    } else if (tab === 'drivers') {
      this.driversVisible = true;
    } else if (tab === 'requests') {
      this.requestsVisible = true;
    }
  }

  setTab(tab: TabType): void {
    this.showTab(tab);
  }

  isCustomersTab(): boolean { return this.activeTab === 'customers'; }
  isDriversTab(): boolean { return this.activeTab === 'drivers'; }
  isRequestsTab(): boolean { return this.activeTab === 'requests'; }

  onRegisterNewDriver() {
    this.router.navigate(['/register-driver']);
  }
}
