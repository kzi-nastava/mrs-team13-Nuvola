import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SupportChatComponent } from '../support.chat.component/support.chat.component';
import { AuthService } from '../../auth/services/auth.service';

@Component({
  selector: 'app-user-chat-page',
  imports: [CommonModule, SupportChatComponent],
  templateUrl: './user.chat.page.html',
  styleUrl: './user.chat.page.css',
})
export class UserChatPage {
  myId: number;

  constructor(private auth: AuthService) {
    this.myId = this.auth.getUserId() ?? -1;
    if (this.myId === -1) {
      console.warn('UserChatPage: no userId found in auth service');
    }
  }
}
