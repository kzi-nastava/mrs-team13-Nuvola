import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, ToastNotification } from '../services/notification.service';

@Component({
  selector: 'app-toast-component',
  imports: [CommonModule],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.css',
})
export class ToastComponent {
  toasts$ : any;

  constructor(public notify: NotificationService) {
    this.toasts$ = this.notify.toasts$;
  }

  trackById(_: number, t: ToastNotification) {
    return t.id;
  }
}
