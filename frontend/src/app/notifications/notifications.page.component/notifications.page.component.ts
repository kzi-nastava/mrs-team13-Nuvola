import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ChangeDetectorRef, Component, Inject, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { NotificationDTO } from '../model/notification';
import { environment } from '../../env/enviroment';
import { AuthService } from '../../auth/services/auth.service';
import { Router } from '@angular/router';


@Component({
  selector: 'app-notifications-page-component',
  imports: [CommonModule],
  templateUrl: './notifications.page.component.html',
  styleUrl: './notifications.page.component.css',
})
export class NotificationsPageComponent implements OnInit, OnDestroy {
  userId? : number | null;

  loading = false;
  errorMessage: string | null = null;

  notifications: NotificationDTO[] = [];

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private router:Router,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  async ngOnInit() {
    this.userId= this.authService.getUserId();
    const id = this.userId;
    if (id == null) {
      this.errorMessage = 'Nije pronađen userId (token/localStorage).';
      return;
    }

    this.fetchNotifications(id);
    
  }

  ngOnDestroy(): void {
  }

  refresh() {
    const id = this.userId;
    if (id == null) return;
    this.fetchNotifications(id);
  }

  trackByIndex = (i: number) => i;

  badgeClass(type: string) {
    const x = (type ?? '').toLowerCase();
    if (x.includes('panic')) return 'badge badge--panic';
    if (x.includes('rideended')) return 'badge badge--ended';
    if (x.includes('rideapproved')) return 'badge badge--approved';
    if (x.includes('novehicle')) return 'badge badge--warn';

    if (x.includes('novehicleavailable')) return 'badge badge--novehicleavailable';
    if (x.includes('rideapproved')) return 'badge badge--rideapproved';
    if (x.includes('youareassignedtoride')) return 'badge badge--youareassignedtoride';
    if (x.includes('ridereminder')) return 'badge badge--ridereminder';
    if (x.includes('linkedpassanger')) return 'badge badge--linkedpassanger';
    if (x.includes('rideended')) return 'badge badge--rideended';
    if (x.includes('panic')) return 'badge badge--panic';
    return 'badge';
  }

  displayType(type: string) {
    return (type ?? 'Notification').replace(/_/g, ' ');
  }

  private fetchNotifications(userId: number) {
    this.loading = true;
    this.errorMessage = null;

    this.http
      .get<NotificationDTO[]>(`${environment.apiHost}/api/notifications/${userId}`)
      .subscribe({
        next: (list) => {
          this.notifications = Array.isArray(list) ? list : [];
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage =
            err?.error?.message ??
            err?.message ??
            'Error while loading notifications.';
        },
      });
  }
  goToPanic() {
  this.router.navigate(['/admin/panic']);
}




}
