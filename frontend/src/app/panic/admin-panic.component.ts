import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminPanicFeedService } from './admin-panic-feed.service';
import { PanicSoundService } from './panic-sound.service';
import { PanicNotification } from './panic.model';

@Component({
  selector: 'app-admin-panic',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-panic.component.html',
  styleUrl: './admin-panic.component.css',
})
export class AdminPanicComponent implements OnInit {
  items = signal<PanicNotification[]>([]);
  lastCount = 0;

  soundEnabled = computed(() => this.sound.isEnabled());

  constructor(private feed: AdminPanicFeedService, private sound: PanicSoundService) {}

  ngOnInit() {
    this.feed.items$.subscribe(list => {
      // detektuj da li se pojavilo novo (seen=false)
      const hasNew = list.some(x => x.seen === false);

      this.items.set(list);

      if (hasNew && this.sound.isEnabled()) {
        this.sound.play();
      }
    });

    this.feed.startPolling(3000);
  }

  enableSound() {
    this.sound.enable();
  }

  markSeen(item: PanicNotification) {
    this.feed.markSeen(item.panicId);
  }

  markAllSeen() {
    this.feed.markAllSeen();
  }

  trackById(_i: number, x: PanicNotification) {
    return x.panicId;
  }
}
