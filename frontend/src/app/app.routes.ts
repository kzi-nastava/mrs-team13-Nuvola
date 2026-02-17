import { Routes } from '@angular/router';
import { DriverRideHistoryComponent } from './rides/driver.ride.history.component/driver.ride.history.component';
import { LoginComponent } from './auth/login.component/login.component';
import { UsersComponent } from './users/users.component/users.component';
import { RegisterDriversComponent } from './users/register.drivers.component/register.drivers.component';
import { AccountComponent } from './layout/account/account.component';
import { DriverAccountComponent } from './layout/driver.account.component/driver.account.component';
import { RegisterComponent } from './auth/register.component/register.component';
import { ForgotPasswordComponent } from './auth/forgot.password.component/forgot.password.component';
import { ResetPasswordComponent } from './auth/reset.password.component/reset.password.component';
import {DriverPasswordComponent} from './auth/driver.password.component/driver.password.component';
//port { EstimateFormComponent } from './rides/estimate.form.component/estimate.form.component';
import { CancelRideComponent } from './rides/cancel-ride.component/cancel-ride.component';
import { AdminPanicComponent } from './panic/admin-panic.component';
import { DriverRidesComponent } from './rides/driver.rides.component/driver.rides.component';
import { LogedinPageComponent } from './logedin.homepage/logedin.page.component/logedin.page.component';
import { HomepageComponent } from './homepage/homepage.component/homepage.component';
import { RideTrackingComponent } from './rides/ride.tracking.component/ride.tracking.component';
import { GradingComponent } from './rides/grading.component/grading.component';
import { ChangePasswordComponent } from './layout/change.password.component/change.password.component';
import { EstimateFormComponent } from './rides/estimate.form.component/estimate.form.component';
import { EstimateResultComponent } from './rides/estimate-result/estimate-result';
import { AuthGuard } from './auth/services/auth.guard';
import { ScheduledRideStartComponent } from './rides/scheduled.ride.start.component/scheduled.ride.start.component';
import { ActivateEmailComponent } from './auth/activate.email.component/activate.email.component';
import { NotificationsPageComponent } from './notifications/notifications.page.component/notifications.page.component';
import { RideHistoryComponent } from './history.ride.registereduser.component/history.ride.registereduser.component';

export const routes: Routes = [
    {path: '', component: LoginComponent },
    {path: 'users', component: UsersComponent },
    {path: 'ride-history/:username', component: DriverRideHistoryComponent },
    { path: 'account-settings', component: AccountComponent },
    {path: 'login', component: LoginComponent },
    {path: 'register', component: RegisterComponent },
    { path: 'forgot-password', component: ForgotPasswordComponent },
    {path: 'reset-password', component: ResetPasswordComponent },
    {path: 'driver-set-password', component: DriverPasswordComponent},
    //{path: 'estimate', component: EstimateFormComponent},
    {path:'rides/:id/cancel', component:CancelRideComponent},
    {path:'admin/panic', component: AdminPanicComponent},
    {path: 'register-driver', component: RegisterDriversComponent },
    {path: 'logedin-home/:username', component: LogedinPageComponent },
    {path: 'homepage', component: HomepageComponent },
    {path: 'ride-tracking/:rideId', component: RideTrackingComponent },
    {path:'grading/:rideId', component: GradingComponent },
    {path: 'driver-rides/:username', component: DriverRidesComponent, canActivate: [AuthGuard],
    data: {role: ['ROLE_REGISTERED_USER', 'ROLE_DRIVER']} },
    {path: 'activate-account', component: ResetPasswordComponent},
    { path: 'change-password', component: ChangePasswordComponent },
    { path: 'driver-account', component: DriverAccountComponent },
    { path: 'estimate', component: EstimateFormComponent },
    { path: 'estimate/result', component: EstimateResultComponent },
    { path: 'scheduled-ride-start/:rideId', component: ScheduledRideStartComponent },
    { path: 'activate', component: ActivateEmailComponent },
    { path: 'notifications', component: NotificationsPageComponent },
    { path: 'ride-history', component:RideHistoryComponent, canActivate: [AuthGuard], data:{role: ['ROLE_REGISTERED_USER']}}
];
