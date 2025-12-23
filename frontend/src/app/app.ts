import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DriverRideHistoryComponent } from './rides/driver.ride.history.component/driver.ride.history.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, DriverRideHistoryComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
