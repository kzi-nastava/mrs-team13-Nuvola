import { Component } from '@angular/core';
import { NavBarComponent } from './layout/nav-bar/nav-bar.component';
import { AccountComponent } from './layout/account/account.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [NavBarComponent, AccountComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {}
