import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DriverRideHistoryComponent } from './rides/driver.ride.history.component/driver.ride.history.component';
import { NavBarComponent } from './layout/nav-bar/nav-bar.component';
import { AccountComponent } from './layout/account/account.component';
import { LoginComponent } from './auth/login.component/login.component';
import { ForgotPasswordComponent } from './auth/forgot.password.component/forgot.password.component';
import { RegisterComponent } from './auth/register.component/register.component';
import { ResetPasswordComponent } from './auth/reset.password.component/reset.password.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, DriverRideHistoryComponent, NavBarComponent, AccountComponent, LoginComponent, ForgotPasswordComponent, RegisterComponent, ResetPasswordComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {}
