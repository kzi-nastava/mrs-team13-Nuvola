import { CommonModule } from '@angular/common';
import { Component, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LocationModel } from '../../logedin.homepage/models/location.model';
import { debounceTime, distinctUntilChanged, of, Subscription, switchMap } from 'rxjs';
import { GeocodingService } from '../../logedin.homepage/services/geocoding.service';
import { Route } from '@angular/router';
import { RouteDataService } from '../service/route.data.service';

@Component({
  selector: 'app-route-panel',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './route.panel.html',
  styleUrl: './route.panel.css',
})
export class RoutePanel implements OnInit, OnDestroy{
  form: FormGroup;

  fromSuggestions: LocationModel[] = [];
  toSuggestions: LocationModel[] = [];

  toastMessage: string = '';
  private toastTimeout: any = null;

  @Output() cleared = new EventEmitter<void>();


  private subs = new Subscription();

  constructor(
    private fb: FormBuilder,
    private geocoding: GeocodingService,
    private routeDataService: RouteDataService
  ) {
    this.form = this.fb.group({
      from: ['', Validators.required],
      to: ['', Validators.required],
    });
  }

  ngOnInit(): void {

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

    this.subs.add(fromSub);
    this.subs.add(toSub);
    

    const syncFrom = this.routeDataService.from$.subscribe((loc) => {
      if (loc) {
        this.form.patchValue({ from: loc.address }, { emitEvent: false });
      }
    });

    const syncTo = this.routeDataService.to$.subscribe((loc) => {
      if (loc) {
        this.form.patchValue({ to: loc.address }, { emitEvent: false });
      }
    });

    this.subs.add(syncFrom);
    this.subs.add(syncTo);
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  // suggestions

  selectFrom(s: LocationModel) {
    this.routeDataService.setFrom(s);
    this.form.patchValue({ from: s.address }, { emitEvent: false });
    this.fromSuggestions = [];
  }

  selectTo(s: LocationModel) {
    this.routeDataService.setTo(s);
    this.form.patchValue({ to: s.address }, { emitEvent: false });
    this.toSuggestions = [];
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
      if (res.length > 0) this.routeDataService.setFrom(res[0]);
    });

    this.geocoding.search(toText + ' Novi Sad').subscribe((res) => {
      if  (res.length > 0) this.routeDataService.setTo(res[0]);
    });
  }

  clearAll() {
    this.routeDataService.reset();

    this.form.reset({
      from: '',
      to: '',
    });

    this.fromSuggestions = [];
    this.toSuggestions = [];
    this.cleared.emit();
  }
  
}
