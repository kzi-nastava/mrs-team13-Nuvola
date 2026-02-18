import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { environment } from '../../env/enviroment';
import { NotificationDTO } from '../model/notification';
import { NotificationService } from './notification.service';


@Injectable({
  providedIn: 'root',
})
export class NotificationSocketService {
  private client?: Client;
  private sub?: StompSubscription;

  constructor(
    private toast: NotificationService,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  /** Connect once and subscribe to /topic/notifications/{userId} */
  async connectForUserId(userId: number | string) {
    const topic = `/topic/notifications/${userId}`;
    await this.connectAndSubscribe(topic);
  }

  async connectAndSubscribe(topic: string) {
    if (!isPlatformBrowser(this.platformId)) return;
    if (this.client?.active) return;

    // polyfill za "global" (ako SockJS/deps zatraže)
    (globalThis as any).global ??= globalThis;

    const sockjsMod: any = await import('sockjs-client');
    const SockJS = sockjsMod.default ?? sockjsMod;

    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.apiHost + '/ws'),
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => {},

      onConnect: () => {
        try { this.sub?.unsubscribe(); } catch {}
        this.sub = this.client!.subscribe(topic, (msg: IMessage) => this.onMessage(msg));
      },
      onStompError: (frame) => console.error('STOMP error', frame),
    });

    this.client.activate();
  }

  disconnect() {
    try { this.sub?.unsubscribe(); } catch {}
    this.sub = undefined;

    try { this.client?.deactivate(); } catch {}
    this.client = undefined;
  }

  private onMessage(msg: IMessage) {
    let dto: NotificationDTO | null = null;
    try {
      dto = JSON.parse(msg.body);
    } catch {
      // fallback ako backend nekad pošalje plain string
      this.toast.show(msg.body, 'RideReminder', 3000);
      return;
    }
    if (dto == null) return;
    const type = this.mapType(dto.type);
    this.toast.show(dto.message, type, 3000, dto.title);
  }

  private mapType(t: string) {
    const x = (t ?? '').toLowerCase();
    if (x.includes('novehicleavailable')) return 'NoVehicleAvailable' as const;
    if (x.includes('rideapproved')) return 'RideApproved' as const;
    if (x.includes('youareassignedtoride')) return 'YouAreAssignedToRide' as const;
    if (x.includes('ridereminder')) return 'RideReminder' as const;
    if (x.includes('linkedpassanger')) return 'LinkedPassanger' as const;
    if (x.includes('rideended')) return 'RideEnded' as const;
    if (x.includes('panic')) return 'PANIC' as const;
    return 'RideReminder' as const;
  }
}
