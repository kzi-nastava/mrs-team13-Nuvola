import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastType = 'info' | 'success' | 'warning' | 'error' | 'RideReminder';

export interface ToastNotification {
  id: string;
  title?: string;
  message: string;
  type: ToastType;
  durationMs: number;
  createdAt: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private toastsSubject = new BehaviorSubject<ToastNotification[]>([]);
  toasts$ = this.toastsSubject.asObservable();

  show(message: string, type: ToastType = 'info', durationMs = 3000, title?: string) {
    const id = crypto?.randomUUID?.() ?? Math.random().toString(36).slice(2);

    const toast: ToastNotification = {
      id,
      title,
      message,
      type,
      durationMs,
      createdAt: Date.now(),
    };

    this.toastsSubject.next([...this.toastsSubject.value, toast]);
    window.setTimeout(() => this.dismiss(id), durationMs);
  }

  dismiss(id: string) {
    this.toastsSubject.next(this.toastsSubject.value.filter(t => t.id !== id));
  }

  clear() {
    this.toastsSubject.next([]);
  }
}