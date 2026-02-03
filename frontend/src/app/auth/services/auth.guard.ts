import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  Router,
} from '@angular/router';
import {AuthService} from './auth.service';


@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
  ): boolean {
    const userRole :string = this.authService.user$.getValue();

    if (userRole == null) {
      this.router.navigate(['login']);
      return false;
    }
    if (!route.data['role'].includes(userRole)) {
      this.router.navigate(['home']);
      return false;
    }
    return true;
  }
}
