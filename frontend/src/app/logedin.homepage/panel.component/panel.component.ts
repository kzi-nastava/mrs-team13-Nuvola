import { Component, OnDestroy, OnInit, Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { debounceTime, distinctUntilChanged, Subscription, switchMap, of } from 'rxjs';

import { RideOrderService, VehicleType } from '../services/ride-order.service';
import { GeocodingService } from '../services/geocoding.service';
import { LocationModel } from '../models/location.model';

@Component({
  selector: 'app-panel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './panel.component.html',
  styleUrls: ['./panel.component.css']
})
export class PanelComponent implements OnInit, OnDestroy, OnChanges  {
  form: FormGroup;

  fromSuggestions: LocationModel[] = [];
  toSuggestions: LocationModel[] = [];
  stops: LocationModel[] = [];
  passengers: string[] = [];

  toastMessage: string = '';
  private toastTimeout: any = null;

  scheduledOptions: { value: string; label: string }[] = [];

  @Output() rideOrdered = new EventEmitter<string>();
  @Output() openFavorites = new EventEmitter<void>();
  @Output() cleared = new EventEmitter<void>();
  @Input() favoriteRouteToLoad: any = null;


  private subs = new Subscription();

  constructor(
    private fb: FormBuilder,
    private geocoding: GeocodingService,
    private rideOrder: RideOrderService
  ) {
    this.form = this.fb.group({
      from: ['', Validators.required],
      to: ['', Validators.required],
      stopInput: [''],
      passengerInput: [''],

      vehicleType: ['standard', Validators.required],
      babySeat: [false],
      petFriendly: [false],

      rideTimeMode: ['now', Validators.required],
      scheduledTime: [''],
    });
  }

  ngOnInit(): void {
    this.buildScheduleOptions();

    const fromSub = this.form.get('from')!.valueChanges
      .pipe(
        debounceTime(150),
        distinctUntilChanged(),
        switchMap((value: string) => {
          if (!value || value.length < 3) return of([]);
          return this.geocoding.search(value + ' Novi Sad');
        })
      )
      .subscribe((results) => {
        this.fromSuggestions = results;
      });

    const toSub = this.form.get('to')!.valueChanges
      .pipe(
        debounceTime(150),
        distinctUntilChanged(),
        switchMap((value: string) => {
          if (!value || value.length < 3) return of([]);
          return this.geocoding.search(value + ' Novi Sad');
        })
      )
      .subscribe((results) => {
        this.toSuggestions = results;
      });

    const modeSub = this.form.get('rideTimeMode')!.valueChanges.subscribe((mode: 'now' | 'scheduled') => {
      const ctrl = this.form.get('scheduledTime')!;

      if (mode === 'scheduled') {
        ctrl.setValidators([Validators.required]);
        if (!ctrl.value && this.scheduledOptions.length > 0) {
          ctrl.setValue(this.scheduledOptions[0].value, { emitEvent: false });
        }
      } else {
        ctrl.clearValidators();
        ctrl.setValue('', { emitEvent: false });
        ctrl.setErrors(null);
      }

      ctrl.updateValueAndValidity({ emitEvent: false });
    });

    const vehicleSub = this.form.get('vehicleType')!.valueChanges.subscribe((t: VehicleType) => {
      this.rideOrder.setVehicleType(t);
    });

    const stopsSub = this.rideOrder.stops$.subscribe((s: LocationModel[]) => this.stops = s);
    const passSub = this.rideOrder.passengers$.subscribe((p: string[]) => this.passengers = p);

    this.subs.add(fromSub);
    this.subs.add(toSub);
    this.subs.add(modeSub);
    this.subs.add(vehicleSub);
    this.subs.add(stopsSub);
    this.subs.add(passSub);

    const syncFrom = this.rideOrder.from$.subscribe((loc) => {
      if (loc) {
        this.form.patchValue({ from: loc.address }, { emitEvent: false });
      }
    });

    const syncTo = this.rideOrder.to$.subscribe((loc) => {
      if (loc) {
        this.form.patchValue({ to: loc.address }, { emitEvent: false });
      }
    });

    this.subs.add(syncFrom);
    this.subs.add(syncTo);

    this.rideOrder.setVehicleType('standard');
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  // favorite routes 
    
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['favoriteRouteToLoad'] && this.favoriteRouteToLoad) {
      this.applyFavoriteRoute(this.favoriteRouteToLoad);
    }
  }

  private applyFavoriteRoute(route: { from: LocationModel; to: LocationModel; stops: LocationModel[] }) {


    this.form.patchValue({
      from: route.from.address,
      to: route.to.address,
      stopInput: ''
    }, { emitEvent: false });

    this.fromSuggestions = [];
    this.toSuggestions = [];

    this.rideOrder.setStops(route.stops ?? []);

    this.geocoding.search(route.from.address + ' Novi Sad').subscribe(res => {
      if (res.length > 0) this.rideOrder.setFrom(res[0]);
    });

    this.geocoding.search(route.to.address + ' Novi Sad').subscribe(res => {
      if (res.length > 0) this.rideOrder.setTo(res[0]);
    });

    (route.stops ?? []).forEach((stop, index) => {
      this.geocoding.search(stop.address + ' Novi Sad').subscribe(res => {
        if (res.length > 0) {
          const current = this.rideOrder.getStops();
          const copy = [...current];

          copy[index] = res[0];
          this.rideOrder.setStops(copy);
        }
      });
    });
  }




  get isScheduled(): boolean {
    return this.form.value.rideTimeMode === 'scheduled';
  }

  // time schedule

  private roundUpToNext5Minutes(d: Date): Date {
    const copy = new Date(d);
    copy.setSeconds(0);
    copy.setMilliseconds(0);

    const minutes = copy.getMinutes();
    const remainder = minutes % 5;

    if (remainder !== 0) {
      copy.setMinutes(minutes + (5 - remainder));
    }
    return copy;
  }

  private formatTimeLabel(d: Date): string {
    const hh = d.getHours().toString().padStart(2, '0');
    const mm = d.getMinutes().toString().padStart(2, '0');

    const today = new Date();
    const isTomorrow =
      d.getDate() !== today.getDate() ||
      d.getMonth() !== today.getMonth() ||
      d.getFullYear() !== today.getFullYear();

    return `${hh}:${mm}${isTomorrow ? ' (tomorrow)' : ''}`;
  }

  private toIsoValue(d: Date): string {
    return d.toISOString();
  }

  private buildScheduleOptions() {
    this.scheduledOptions = [];

    const now = new Date();
    const start = this.roundUpToNext5Minutes(now);
    const end = new Date(now.getTime() + 5 * 60 * 60 * 1000);

    let cur = new Date(start);

    while (cur <= end) {
      this.scheduledOptions.push({
        value: this.toIsoValue(cur),
        label: this.formatTimeLabel(cur),
      });

      cur = new Date(cur.getTime() + 5 * 60 * 1000);
    }
  }

  // suggestions

  selectFrom(s: LocationModel) {
    this.rideOrder.setFrom(s);
    this.form.patchValue({ from: s.address }, { emitEvent: false });
    this.fromSuggestions = [];
  }

  selectTo(s: LocationModel) {
    this.rideOrder.setTo(s);
    this.form.patchValue({ to: s.address }, { emitEvent: false });
    this.toSuggestions = [];
  }

  // additional stops

  addStopFromInput() {
    const text = this.form.value.stopInput?.trim();
    if (!text) return;

    this.geocoding.search(text + ' Novi Sad').subscribe(res => {
      if (res.length === 0) return;

      this.rideOrder.addStop(res[0]);
      this.form.patchValue({ stopInput: '' }, { emitEvent: false });
    });
  }

  removeStop(i: number) {
    this.rideOrder.removeStop(i);
  }

  // passengers

  addPassengerFromInput() {
    const email = this.form.value.passengerInput?.trim();
    if (!email) return;

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) return;

    this.rideOrder.addPassenger(email);
    this.form.patchValue({ passengerInput: '' }, { emitEvent: false });
  }

  removePassenger(i: number) {
    this.rideOrder.removePassenger(i);
  }

  // buttons

  calculateRoute() {
    if (this.form.get('from')?.invalid || this.form.get('to')?.invalid) {
      this.form.get('from')?.markAsTouched();
      this.form.get('to')?.markAsTouched();
      return;
    }

    const fromText = this.form.value.from;
    const toText = this.form.value.to;

    this.geocoding.search(fromText + ' Novi Sad').subscribe((res) => {
      if (res.length > 0) this.rideOrder.setFrom(res[0]);
    });

    this.geocoding.search(toText + ' Novi Sad').subscribe((res) => {
      if  (res.length > 0) this.rideOrder.setTo(res[0]);
    });
  }

  onOpenFavorites() {
    this.openFavorites.emit();
  }

  orderRide() {
    this.form.get('from')?.markAsTouched();
    this.form.get('to')?.markAsTouched();

    const fromValue = this.form.get('from')?.value?.trim();
    const toValue = this.form.get('to')?.value?.trim();

    if (!fromValue || !toValue) {
      return; 
    }

    this.rideOrdered.emit(
      "Ride request submitted. You'll receive a notification with ride details once a driver is assigned."
    );
  }


  clearAll() {
    this.rideOrder.reset();

    this.form.reset({
      from: '',
      to: '',
      stopInput: '',
      passengerInput: '',
      vehicleType: 'standard',
      babySeat: false,
      petFriendly: false,
      rideTimeMode: 'now',
      scheduledTime: '',
    });

    this.fromSuggestions = [];
    this.toSuggestions = [];
    this.cleared.emit();
  }
  

}
