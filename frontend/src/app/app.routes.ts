import { Routes } from '@angular/router';
import { DriverRideHistoryComponent } from './rides/driver.ride.history.component/driver.ride.history.component';
import { LoginComponent } from './auth/login.component/login.component';
import { AccountComponent } from './layout/account/account.component';
import { RegisterComponent } from './auth/register.component/register.component';
import { ForgotPasswordComponent } from './auth/forgot.password.component/forgot.password.component';
import { ResetPasswordComponent } from './auth/reset.password.component/reset.password.component';

export const routes: Routes = [
    {path: '', component: LoginComponent },
    {path: 'ride-history/:username', component: DriverRideHistoryComponent },
    {path: 'account-settings/:username', component: AccountComponent },
    {path: 'login', component: LoginComponent },
    {path: 'register', component: RegisterComponent },
    {path: 'forgot-password/:username', component: ForgotPasswordComponent },
    {path: 'reset-password', component: ResetPasswordComponent },
];
