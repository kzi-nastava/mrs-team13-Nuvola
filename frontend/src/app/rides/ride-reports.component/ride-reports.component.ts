import {
  Component,
  ViewChild,
  ElementRef,
  OnDestroy,
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../auth/services/auth.service';
import { RideReportsService, RideReportResponse } from './service/ride-reports.service';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-ride-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ride-reports.component.html',
  styleUrl: './ride-reports.component.css',
})
export class RideReportsComponent implements OnDestroy {
  startDate: string = '';
  endDate: string = '';

  adminTarget: 'ALL_DRIVERS' | 'ALL_CUSTOMERS' | 'ONE_DRIVER' | 'ONE_CUSTOMER' = 'ALL_DRIVERS';
  targetEmail: string = '';

  loading = false;
  error: string | null = null;
  reportData: RideReportResponse | null = null;

  private ridesChart?: Chart;
  private kmChart?: Chart;
  private moneyChart?: Chart;

  @ViewChild('ridesCanvas') ridesCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('kmCanvas') kmCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('moneyCanvas') moneyCanvas!: ElementRef<HTMLCanvasElement>;

  constructor(
    public authService: AuthService,
    private reportsService: RideReportsService,
    private cdr: ChangeDetectorRef
  ) {}

  get role(): string {
    return this.authService.getRole() ?? '';
  }

  get isAdmin(): boolean {
    return this.role === 'ROLE_ADMIN';
  }

  get isDriver(): boolean {
    return this.role === 'ROLE_DRIVER';
  }

  get isRegisteredUser(): boolean {
    return this.role === 'ROLE_REGISTERED_USER';
  }

  get needsEmail(): boolean {
    return this.adminTarget === 'ONE_DRIVER' || this.adminTarget === 'ONE_CUSTOMER';
  }

  get moneyLabel(): string {
    if (this.isAdmin) {
      return (this.adminTarget === 'ALL_DRIVERS' || this.adminTarget === 'ONE_DRIVER')
        ? 'Money Earned (RSD)'
        : 'Money Spent (RSD)';
    }
    return this.isDriver ? 'Money Earned (RSD)' : 'Money Spent (RSD)';
  }

  generate(): void {
    if (!this.startDate || !this.endDate) {
      this.error = 'Please select start and end date.';
      return;
    }
    if (new Date(this.startDate) > new Date(this.endDate)) {
      this.error = 'Start date must be before end date.';
      return;
    }
    if (this.needsEmail && !this.targetEmail) {
      this.error = 'Please enter an email address.';
      return;
    }

    this.loading = true;
    this.error = null;
    this.reportData = null;
    this.destroyCharts();

    const obs = this.isAdmin
      ? this.reportsService.getAdminReport(this.startDate, this.endDate, this.adminTarget, this.targetEmail || undefined)
      : this.reportsService.getMyReport(this.startDate, this.endDate);

    obs.subscribe({
      next: (data) => {
        this.reportData = data;
        this.loading = false;
        // Force Angular to run change detection and update DOM
        this.cdr.detectChanges();
        // setTimeout(0) lets the browser paint the now-visible canvases before Chart.js draws
        setTimeout(() => {
          this.buildCharts(data);
        }, 0);
      },
      error: (err) => {
        console.error('[RideReports] Error:', err);
        this.loading = false;
        this.error = err?.error?.message || err?.message || 'Error loading report.';
        this.cdr.detectChanges();
      },
    });
  }

  private buildCharts(data: RideReportResponse): void {
    this.destroyCharts();

    if (!this.ridesCanvas?.nativeElement) {
      console.error('[RideReports] Canvas not in DOM!');
      return;
    }

    const labels = data.data.map((d) => d.date);
    const rides = data.data.map((d) => d.rideCount);
    const km = data.data.map((d) => d.totalKm);
    const money = data.data.map((d) => d.totalMoney);

    this.ridesChart = this.createChart(
      this.ridesCanvas.nativeElement,
      labels, rides,
      'Number of Rides', '#4f46e5', 'rgba(79,70,229,0.15)'
    );

    this.kmChart = this.createChart(
      this.kmCanvas.nativeElement,
      labels, km,
      'Kilometers Traveled', '#059669', 'rgba(5,150,105,0.15)'
    );

    this.moneyChart = this.createChart(
      this.moneyCanvas.nativeElement,
      labels, money,
      this.moneyLabel, '#d97706', 'rgba(217,119,6,0.15)'
    );
  }

  private createChart(
    canvas: HTMLCanvasElement,
    labels: string[],
    data: number[],
    label: string,
    borderColor: string,
    backgroundColor: string
  ): Chart {
    return new Chart(canvas, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label,
          data,
          borderColor,
          backgroundColor,
          fill: true,
          tension: 0.3,
          pointRadius: 4,
          pointHoverRadius: 6,
        }],
      },
      options: {
        responsive: true,
        plugins: { legend: { display: true } },
        scales: {
          x: { title: { display: true, text: 'Date' } },
          y: { title: { display: true, text: label }, beginAtZero: true },
        },
      },
    });
  }

  private destroyCharts(): void {
    this.ridesChart?.destroy();
    this.kmChart?.destroy();
    this.moneyChart?.destroy();
    this.ridesChart = undefined;
    this.kmChart = undefined;
    this.moneyChart = undefined;
  }

  ngOnDestroy(): void {
    this.destroyCharts();
  }
}