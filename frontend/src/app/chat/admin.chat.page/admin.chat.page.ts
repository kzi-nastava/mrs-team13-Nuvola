import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { SupportChatComponent } from '../support.chat.component/support.chat.component';
import { AuthService } from '../../auth/services/auth.service';

@Component({
  selector: 'app-admin-chat-page',
  imports: [CommonModule, SupportChatComponent, RouterModule],
  templateUrl: './admin.chat.page.html',
  styleUrl: './admin.chat.page.css',
})
export class AdminChatPage implements OnInit, OnDestroy {
  adminId!: number;
  receiverUserId!: number;

  private sub?: Subscription;

  constructor(private route: ActivatedRoute, private auth: AuthService) {
    this.adminId = this.auth.getUserId() ?? -1;
    if (this.adminId === -1) {
      console.warn('AdminChatPage: no userId found in auth service');
    }
  }

  ngOnInit(): void {
    this.sub = this.route.paramMap.subscribe((pm) => {
      const v = pm.get('userId');
      this.receiverUserId = v ? Number(v) : NaN;
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  get receiverValid(): boolean {
    return Number.isFinite(this.receiverUserId);
  }
}
