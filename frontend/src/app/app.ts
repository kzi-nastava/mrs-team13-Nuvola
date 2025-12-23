import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DriverRideHistoryComponent } from './rides/driver.ride.history.component/driver.ride.history.component';
import { NavBarComponent } from './layout/nav-bar/nav-bar.component';
import { AccountComponent } from './layout/account/account.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, DriverRideHistoryComponent, NavBarComponent, AccountComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {}
