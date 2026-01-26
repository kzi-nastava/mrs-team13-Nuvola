import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, timer } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { PanicNotification } from './panic.model';

type RideHistoryItem = {
  id: number;
  startTime?: string;
  endTime?: string;
  panic?: boolean;
  // ako imate još polja, ne smeta
};

@Injectable({ providedIn: 'root' })
export class AdminPanicFeedService {
  private http = inject(HttpClient);
  private baseUrl = '/api';

  private itemsSubject = new BehaviorSubject<PanicNotification[]>([]);
  items$ = this.itemsSubject.asObservable();

  // čuvamo “ključ” već viđenih panic događaja
  private seenKeys = new Set<string>();

  startPolling(ms = 3000) {
    timer(0, ms)
      .pipe(
        switchMap(() =>
          this.http.get<RideHistoryItem[]>(`${this.baseUrl}/admin/rides/history`)
        )
      )
      .subscribe({
        next: (history) => {
          const panicRides = (history ?? []).filter(h => !!h.panic);

          const mapped: PanicNotification[] = panicRides.map(h => {
            const time = h.endTime || h.startTime || new Date().toISOString();
            const key = `${h.id}_${time}`;

            const isNew = !this.seenKeys.has(key);
            if (isNew) this.seenKeys.add(key);

            return {
              panicId: h.id,              // fallback
              rideId: h.id,
              triggeredBy: 'PASSENGER',   // fallback ako backend ne šalje ko je
              time,
              seen: !isNew,
            };
          });

          // sort: newest first
          mapped.sort((a, b) => (a.time < b.time ? 1 : -1));

          this.itemsSubject.next(mapped);
        },
        error: () => {
          // u realnom kodu: toast “ne mogu da povučem”
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
