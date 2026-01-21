import { Routes } from '@angular/router';
import { DriverRideHistoryComponent } from './rides/driver.ride.history.component/driver.ride.history.component';
import { LoginComponent } from './auth/login.component/login.component';
import { UsersComponent } from './users/users.component/users.component';
import { RegisterDriversComponent } from './users/register.drivers.component/register.drivers.component';
import { AccountComponent } from './layout/account/account.component';
import { RegisterComponent } from './auth/register.component/register.component';
import { ForgotPasswordComponent } from './auth/forgot.password.component/forgot.password.component';
import { ResetPasswordComponent } from './auth/reset.password.component/reset.password.component';
import { EstimateFormComponent } from './rides/estimate.form.component/estimate.form.component';
import { CancelRideComponent } from './rides/cancel-ride.component/cancel-ride.component';

export const routes: Routes = [
    {path: '', component: LoginComponent },
    {path: 'users/:username', component: UsersComponent },
    {path: 'ride-history/:username', component: DriverRideHistoryComponent },
    {path: 'account-settings/:username', component: AccountComponent },
    {path: 'login', component: LoginComponent },
    {path: 'register', component: RegisterComponent },
    {path: 'forgot-password/:username', component: ForgotPasswordComponent },
    {path: 'reset-password', component: ResetPasswordComponent },
    {path: 'estimate', component: EstimateFormComponent},
    {path:'rides/:id/cancel', component:CancelRideComponent},
    {path: 'register-driver', component: RegisterDriversComponent },
];
