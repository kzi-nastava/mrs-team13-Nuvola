import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../env/enviroment';
import { AdminInboxItemDTO } from '../model/admin.inbox.item';
import { ChatMessageDTO } from '../model/chat.message';
import { PageDTO } from '../model/page';

@Injectable({ providedIn: 'root' })
export class SupportChatApiService {
  private base = `${environment.apiHost}/api/support`;

  constructor(private http: HttpClient) {}

  // USER chat history
  getChatMessagesForUser(userId: number): Observable<ChatMessageDTO[]> {
    return this.http.get<ChatMessageDTO[]>(`${this.base}/chat/${userId}`);
  }

  // ADMIN inbox paging
  getAdminInbox(adminId: number, page: number, size: number): Observable<PageDTO<AdminInboxItemDTO>> {
    const params = new HttpParams()
      .set('adminId', adminId)
      .set('page', page)
      .set('size', size);

    return this.http.get<PageDTO<AdminInboxItemDTO>>(`${this.base}/admin/inbox`, { params });
  }

  /**
   * ⚠️ Admin-u će realno trebati endpoint za istoriju poruka po chatId ili userId.
   * Trenutno /chat/{userId} baca error ako je admin.
   *
   * Predlog backend-a:
   * GET /api/support/admin/chat/{chatId}
   * ili GET /api/support/admin/chat/by-user/{userId}
   */
}