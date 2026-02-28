import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { PricingService } from '../services/pricing.service';
import { VehicleType, VehicleTypePricingDTO } from '../model/pricing.model';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

type RowForm = FormGroup<{
  vehicleType: FormControl<VehicleType>;
  basePrice: FormControl<number>;
}>;


@Component({
  selector: 'app-change-price-component',
  standalone: true,   
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change.price.component.html',
  styleUrl: './change.price.component.css',
})
export class ChangePriceComponent implements OnInit {
  // readonly vehicleTypes: VehicleType[] = ['STANDARD', 'LUXURY', 'VAN'];

  // form = this.fb.group({
  //   rows: this.fb.array<RowForm>([]),
  // });

  // loading = false;
  // saving = false;
  // errorMessage = '';
  // successMessage = '';

  // constructor(private fb: FormBuilder, private pricingService: PricingService) {}

  // get rows(): FormArray<RowForm> {
  //   return this.form.get('rows') as FormArray<RowForm>;
  // }

  // ngOnInit(): void {
  //   this.loadAndEnsureDefaults();
  // }

  readonly vehicleTypes: VehicleType[] = ['STANDARD', 'LUXURY', 'VAN'];

  form: FormGroup<{ rows: FormArray<RowForm> }>;

  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';

  constructor(private fb: FormBuilder, private pricingService: PricingService, private cdr: ChangeDetectorRef) {
    this.form = this.fb.group({
      rows: this.fb.array<RowForm>([]),
    });
  }

  get rows(): FormArray<RowForm> {
    return this.form.controls.rows;
  }

  ngOnInit(): void {
    this.loadAndEnsureDefaults();
  }


  private loadAndEnsureDefaults(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.pricingService.getAllVehicleTypePrices().pipe(
      map(list => {
        const mapPrices = new Map<VehicleType, string>();
        for (const item of list) mapPrices.set(item.vehicleType, item.basePrice);
        return mapPrices;
      }),
      switchMap(mapPrices => {
        const missing = this.vehicleTypes.filter(t => !mapPrices.has(t));
        if (missing.length === 0) return of(mapPrices);

        return forkJoin(
          missing.map(t =>
            this.pricingService.upsertVehicleTypePrice(t, '0').pipe(
              map((saved) => ({ t, saved })),
              catchError(() => of(null))
            )
          )
        ).pipe(
          map(results => {
            for (const r of results) {
              if (r) mapPrices.set(r.t, r.saved.basePrice);
            }
            for (const t of this.vehicleTypes) {
              if (!mapPrices.has(t)) mapPrices.set(t, '0');
            }
            return mapPrices;
          })
        );
      }),
      map(mapPrices => {
        this.rows.clear();
        for (const t of this.vehicleTypes) {
          const priceStr = mapPrices.get(t) ?? '0';
          const priceNum = Number(priceStr);

          this.rows.push(this.fb.group({
            vehicleType: this.fb.control<VehicleType>(t, { nonNullable: true }),
            basePrice: this.fb.control<number>(
              Number.isFinite(priceNum) ? priceNum : 0,
              { nonNullable: true, validators: [Validators.required, Validators.min(0)] }
            ),
          }));
        }
      }),
      catchError(err => {
        this.errorMessage = 'Cannot load prices.';
        return of(null);
      })
    ).subscribe({
      next: () => {this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  saveRow(i: number): void {
    this.successMessage = '';
    this.errorMessage = '';

    const row = this.rows.at(i);
    if (row.invalid) {
      row.markAllAsTouched();
      return;
    }

    this.saving = true;

    const type = row.controls.vehicleType.value;
    const price = row.controls.basePrice.value;

    this.pricingService.upsertVehicleTypePrice(type, String(price)).pipe(
      catchError(err => {
        this.errorMessage = 'Error saving price.';
        return of(null);
      })
    ).subscribe(res => {
      this.saving = false;
      if (res) this.successMessage = 'Saved.';
      this.cdr.detectChanges();
    });
  }

  saveAll(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;

    const calls = this.rows.controls.map(r => {
      const type = r.controls.vehicleType.value;
      const price = r.controls.basePrice.value;
      return this.pricingService.upsertVehicleTypePrice(type, String(price)).pipe(
        catchError(() => of(null))
      );
    });

    forkJoin(calls).subscribe(() => {
      this.saving = false;
      this.successMessage = 'All prices are saved.';
      this.cdr.detectChanges();
    });
  }
}