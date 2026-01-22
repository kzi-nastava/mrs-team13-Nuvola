import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { LocationModel } from '../models/location.model';

export type VehicleType = 'standard' | 'luxury' | 'van';

@Injectable({ providedIn: 'root' })
export class RideOrderService {
  private fromSubject = new BehaviorSubject<LocationModel | null>(null);
  private toSubject = new BehaviorSubject<LocationModel | null>(null);

  private stopsSubject = new BehaviorSubject<LocationModel[]>([]);
  private passengersSubject = new BehaviorSubject<string[]>([]);

  private vehicleTypeSubject = new BehaviorSubject<VehicleType>('standard');

  from$ = this.fromSubject.asObservable();
  to$ = this.toSubject.asObservable();

  stops$ = this.stopsSubject.asObservable();
  passengers$ = this.passengersSubject.asObservable();

  vehicleType$ = this.vehicleTypeSubject.asObservable();

  getFrom() {
    return this.fromSubject.value;
  }

  getTo() {
    return this.toSubject.value;
  }

  getStops() {
    return this.stopsSubject.value;
  }

  getPassengers() {
    return this.passengersSubject.value;
  }

  getVehicleType(): VehicleType {
    return this.vehicleTypeSubject.value;
  }

  setFrom(loc: LocationModel | null) {
    this.fromSubject.next(loc);
  }

  setTo(loc: LocationModel | null) {
    this.toSubject.next(loc);
  }

  addStop(loc: LocationModel) {
    this.stopsSubject.next([...this.stopsSubject.value, loc]);
  }

  setStops(stops: LocationModel[]) {
  this.stopsSubject.next(stops);
}


  removeStop(index: number) {
    const copy = [...this.stopsSubject.value];
    copy.splice(index, 1);
    this.stopsSubject.next(copy);
  }

  addPassenger(email: string) {
    if (this.passengersSubject.value.includes(email)) return;
    this.passengersSubject.next([...this.passengersSubject.value, email]);
  }

  removePassenger(index: number) {
    const copy = [...this.passengersSubject.value];
    copy.splice(index, 1);
    this.passengersSubject.next(copy);
  }

  setVehicleType(type: VehicleType) {
    this.vehicleTypeSubject.next(type);
  }

  clearStops() {
    this.stopsSubject.next([]);
  }


  reset() {
    this.setFrom(null);
    this.setTo(null);
    this.stopsSubject.next([]);
    this.passengersSubject.next([]);
    this.vehicleTypeSubject.next('standard');
  }
}
