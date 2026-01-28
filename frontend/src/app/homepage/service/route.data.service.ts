import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { LocationModel } from '../../logedin.homepage/models/location.model';

@Injectable({
  providedIn: 'root',
})
export class RouteDataService {
  private fromSubject = new BehaviorSubject<LocationModel | null>(null);
  private toSubject = new BehaviorSubject<LocationModel | null>(null);

  from$ = this.fromSubject.asObservable();
  to$ = this.toSubject.asObservable();

  getFrom() {
    return this.fromSubject.value;
  }

  getTo() {
    return this.toSubject.value;
  }

  setFrom(loc: LocationModel | null) {
    this.fromSubject.next(loc);
  }

  setTo(loc: LocationModel | null) {
    this.toSubject.next(loc);
  }

  reset() {
    this.setFrom(null);
    this.setTo(null);
  }
}
