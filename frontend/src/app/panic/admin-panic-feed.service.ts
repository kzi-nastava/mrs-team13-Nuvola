import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, timer } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { PanicNotification } from './panic.model';
import { environment } from '../env/enviroment';

type PanicDTO = {
  rideId: number;
  driverId: number | null;
  passengerId: number | null;
};

@Injectable({ providedIn: 'root' })
export class AdminPanicFeedService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiHost + '/api';

  private itemsSubject = new BehaviorSubject<PanicNotification[]>([]);
  items$ = this.itemsSubject.asObservable();

  private seenKeys = new Set<string>();

  startPolling(ms = 3000) {
    timer(0, ms)
      .pipe(
        switchMap(() =>
          this.http.get<PanicDTO[]>(`${this.baseUrl}/admin/panic`)
        )
      )
      .subscribe({
        next: (list) => {
          const now = new Date().toISOString();

          const mapped: PanicNotification[] = (list ?? []).map(p => {
            const key = `${p.rideId}_${p.driverId ?? 'x'}_${p.passengerId ?? 'x'}`;
            const isNew = !this.seenKeys.has(key);
            if (isNew) this.seenKeys.add(key);

            return {
              panicId: p.rideId,            
              rideId: p.rideId,
              triggeredBy: (p.driverId != null && p.driverId === p.passengerId) ? 'DRIVER' : 'PASSENGER',
              time: now,
              seen: !isNew,
            };
          });

          
          mapped.sort((a, b) => (a.time < b.time ? 1 : -1));
          this.itemsSubject.next(mapped);
        },
        error: (e) => {
          console.error('PANIC feed error', e);
        }
      });
  }

  markSeen(panicId: number) {
    const cur = this.itemsSubject.value;
    this.itemsSubject.next(cur.map(x => x.panicId === panicId ? { ...x, seen: true } : x));
  }

  markAllSeen() {
    const cur = this.itemsSubject.value;
    this.itemsSubject.next(cur.map(x => ({ ...x, seen: true })));
  }
}
