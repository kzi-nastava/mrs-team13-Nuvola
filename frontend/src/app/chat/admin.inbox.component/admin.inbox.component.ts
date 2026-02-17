import { ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StompSubscription } from '@stomp/stompjs';
import { SupportChatApiService } from '../services/support.chat.api.service';
import { SupportChatWsService } from '../services/support.chat.ws.service';
import { AdminInboxItemDTO } from '../model/admin.inbox.item';
import { ChatMessageDTO } from '../model/chat.message';
import { PageDTO } from '../model/page';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../auth/services/auth.service';

@Component({
  selector: 'app-admin.inbox.component',
  imports: [CommonModule, RouterModule],
  templateUrl: './admin.inbox.component.html',
  styleUrl: './admin.inbox.component.css',
})
export class AdminInboxComponent implements OnInit, OnDestroy {
  adminId!: number;

  page = 0;
  size = 10;

  data?: PageDTO<AdminInboxItemDTO>;
  loading = false;

  private allSub?: StompSubscription;

  selected?: AdminInboxItemDTO;

  constructor(
    private api: SupportChatApiService,
    private ws: SupportChatWsService,
    private auth: AuthService,
    private cdr: ChangeDetectorRef,
    private router: Router,
  ) {
    this.adminId = this.auth.getUserId() ?? -1;
    if (this.adminId === -1) {
      console.warn('AdminChatPage: no userId found in auth service');
    }
  }

  // ngOnInit(): void {
  //   this.ws.connect();
  //   this.load();

  //   this.allSub = this.ws.subscribeAll((_m: ChatMessageDTO) => {
  //     this.page = 0;
  //     this.load();
  //   });
  // }
  ngOnInit(): void {
    void this.init();
  }

  private async init(): Promise<void> {
    // ✅ sačekaj ws connect
    await this.ws.connect();

    this.load();

    // ✅ tek posle connect subscribe
    this.allSub = await this.ws.subscribeAll((_m: ChatMessageDTO) => {
      this.page = 0;
      this.load();
    });
  }

  ngOnDestroy(): void {
    this.allSub?.unsubscribe();
  }

  load(): void {
    this.loading = true;
    this.api.getAdminInbox(this.adminId, this.page, this.size).subscribe({
      next: (res) => {
        this.data = res;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      },
    });
  }

  prev(): void {
    if (this.page <= 0) return;
    this.page--;
    this.load();
  }

  next(): void {
    if (!this.data) return;
    if (this.page >= this.data.totalPages - 1) return;
    this.page++;
    this.load();
  }

  select(item: AdminInboxItemDTO) {
    this.selected = item;
    this.openChat(item);
  }

  openChat(item: AdminInboxItemDTO) {
  this.router.navigate(['/admin/support/chat/', item.userId]);
}
}
