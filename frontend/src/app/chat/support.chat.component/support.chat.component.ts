import { ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StompSubscription } from '@stomp/stompjs';
import { SupportChatApiService } from '../services/support.chat.api.service';
import { SupportChatWsService } from '../services/support.chat.ws.service';
import { AdminSendChatMessageDTO } from '../model/admin.send.chat.message';
import { ChatMessageDTO } from '../model/chat.message';
import { SendChatMessageDTO } from '../model/send.chat.message';

@Component({
  selector: 'app-support-chat-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './support.chat.component.html',
  styleUrl: './support.chat.component.css',
})
export class SupportChatComponent implements OnInit, OnDestroy {
  /**
   * USER MODE:
   * - prosledi userId (owner) i myId (isti taj user)
   * - komponenta subscribe-uje na /topic/chats/users/{userId}
   *
   * ADMIN MODE:
   * - prosledi myId (adminId) + receiverUserId (user sa kojim pričaš)
   * - subscribe ide na /topic/chats/users/{receiverUserId}
   */
  @Input() userId?: number;          // owner user id (za user mode + REST load)
  @Input() myId!: number;            // id onoga ko šalje (user ili admin)
  @Input() isAdmin = false;
  @Input() receiverUserId?: number;  // samo admin: user sa kojim priča

  messages: ChatMessageDTO[] = [];
  draft = '';
  loading = false;

  private chatSub?: StompSubscription;

  constructor(
    private api: SupportChatApiService,
    private ws: SupportChatWsService,
    private cdr: ChangeDetectorRef,
  ) {}

  // ngOnInit(): void {
  //   this.ws.connect();

  //   const targetUserId = this.getTargetUserIdForTopic();
  //   if (targetUserId == null) {
  //     console.warn('SupportChatComponent: missing target userId for subscription');
  //     return;
  //   }

  //   // 1) load istorije
  //   // - tvoj backend endpoint /chat/{userId} radi za user-e (nije admin)
  //   // - admin trenutno nema endpoint za istoriju => preskoči ili dodaj poseban endpoint
  //   if (!this.isAdmin && this.userId != null) {
  //     this.loading = true;
  //     this.api.getChatMessagesForUser(this.userId).subscribe({
  //       next: (msgs) => {
  //         this.messages = msgs;
  //         this.loading = false;
  //         this.subscribe(targetUserId);
  //         queueMicrotask(() => this.scrollToBottom());
  //       },
  //       error: (err) => {
  //         console.error(err);
  //         this.loading = false;
  //         this.subscribe(targetUserId);
  //       },
  //     });
  //   } else {
  //     // admin: TODO kad dodaš endpoint za istoriju, ovde učitaj po receiverUserId (ili chatId)
  //     this.subscribe(targetUserId);
  //   }
  // }

  ngOnInit(): void {
    void this.init();
  }

  private async init(): Promise<void> {
    const targetUserId = this.getTargetUserIdForTopic();
    if (targetUserId == null) {
      console.warn('SupportChatComponent: missing target userId for subscription');
      return;
    }

    //  čekaj da SockJS bude učitan + client kreiran + activate pozvan
    await this.ws.connect();

    // 1) load istorije
    if (!this.isAdmin && this.userId != null) {
      this.loading = true;
      this.api.getChatMessagesForUser(this.userId).subscribe({
        next: (msgs) => {
          this.messages = msgs;
          this.loading = false;
          this.subscribe(targetUserId);
          queueMicrotask(() => this.scrollToBottom());
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);
          this.loading = false;
          this.subscribe(targetUserId);
          this.cdr.detectChanges();
        },
      });
    } else {
      // admin: učitaj isto ako želiš (prosledi targetUserId), ili ostavi bez istorije
      this.loading = true;
      this.api.getChatMessagesForUser(targetUserId).subscribe({
        next: (msgs) => {
          this.messages = msgs;
          this.loading = false;
          this.subscribe(targetUserId);
          queueMicrotask(() => this.scrollToBottom());
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);
          this.loading = false;
          this.subscribe(targetUserId);
          this.cdr.detectChanges(); 
        },
      });
    }
  }



  ngOnDestroy(): void {
    this.chatSub?.unsubscribe();
  }

  private getTargetUserIdForTopic(): number | null {
    if (this.isAdmin) {
      return this.receiverUserId ?? null;
    }
    return this.userId ?? null;
  }

  private async subscribe(targetUserId: number) {
    this.chatSub?.unsubscribe();
    this.chatSub = await this.ws.subscribeChat(targetUserId, (m) => {
      // mala zaštita da ne dupliraš poruke ako backend nekad šalje isto 2x
      if (this.messages.length > 0 && this.messages[this.messages.length - 1].id === m.id) {
        return;
      }
      this.messages = [...this.messages, m];
      queueMicrotask(() => this.scrollToBottom());
      this.cdr.detectChanges();
    });
  }

  send(): void {
    const text = this.draft.trim();
    if (!text) return;

    if (this.isAdmin) {
      if (!this.receiverUserId) {
        console.warn('receiverUserId is required for admin send');
        return;
      }
      const dto: AdminSendChatMessageDTO = {
        senderId: this.myId,
        receiverId: this.receiverUserId,
        content: text,
      };
      this.ws.sendAdminMessage(dto);
    } else {
      const dto: SendChatMessageDTO = {
        senderId: this.myId,
        content: text,
      };
      this.ws.sendUserMessage(dto);
    }

    this.draft = '';
    //this.cdr.detectChanges();
  }

  isMine(m: ChatMessageDTO): boolean {
    return m.senderId === this.myId;
  }

  private scrollToBottom() {
    const el = document.getElementById('chat-scroll');
    if (el) el.scrollTop = el.scrollHeight;
  }
}
