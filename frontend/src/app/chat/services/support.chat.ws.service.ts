// import { Injectable, NgZone } from '@angular/core';
// import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
// import SockJS from 'sockjs-client';
// import { BehaviorSubject } from 'rxjs';
// import { environment } from '../../env/enviroment';
// import { AdminSendChatMessageDTO } from '../model/admin.send.chat.message';
// import { ChatMessageDTO } from '../model/chat.message';
// import { SendChatMessageDTO } from '../model/send.chat.message';

// @Injectable({ providedIn: 'root' })
// export class SupportChatWsService {
//   private client?: Client;

//   private connected$ = new BehaviorSubject<boolean>(false);

//   constructor(private zone: NgZone) {}

//   isConnected() {
//     return this.connected$.asObservable();
//   }

//   connect(): void {
//     //if (this.client && this.client.connected) return;
//     if (this.client && (this.client.connected || this.client.active)) return;

//     const socketUrl = `${environment.apiHost}/ws`;
//     this.client = new Client({
//       webSocketFactory: () => new SockJS(socketUrl),
//       reconnectDelay: 2000,
//       debug: () => {},
//     });

//     this.client.onConnect = () => {
//       this.zone.run(() => this.connected$.next(true));
//     };

//     this.client.onDisconnect = () => {
//       this.zone.run(() => this.connected$.next(false));
//     };

//     this.client.onStompError = (frame) => {
//       console.error('STOMP error', frame.headers['message'], frame.body);
//     };

//     this.client.activate();
//   }

//   disconnect(): void {
//     if (!this.client) return;
//     this.client.deactivate();
//     this.client = undefined;
//     this.connected$.next(false);
//   }

//   subscribeChat(userId: number, onMessage: (m: ChatMessageDTO) => void): StompSubscription {
//     this.ensureConnected();
//     return this.client!.subscribe(`/topic/chats/users/${userId}`, (msg: IMessage) => {
//       const parsed = JSON.parse(msg.body) as ChatMessageDTO;
//       this.zone.run(() => onMessage(parsed));
//     });
//   }

//   subscribeAll(onMessage: (m: ChatMessageDTO) => void): StompSubscription {
//     this.ensureConnected();
//     return this.client!.subscribe(`/topic/chats/users/all`, (msg: IMessage) => {
//       const parsed = JSON.parse(msg.body) as ChatMessageDTO;
//       this.zone.run(() => onMessage(parsed));
//     });
//   }

//   sendUserMessage(dto: SendChatMessageDTO): void {
//     this.ensureConnected();
//     this.client!.publish({
//       destination: `/app/chats/send`,
//       body: JSON.stringify(dto),
//     });
//   }

//   sendAdminMessage(dto: AdminSendChatMessageDTO): void {
//     this.ensureConnected();
//     this.client!.publish({
//       destination: `/app/admin/chats/send`,
//       body: JSON.stringify(dto),
//     });
//   }

//   private ensureConnected() {
//     if (!this.client) this.connect();
//     if (!this.client!.connected) {
//       // ako još nije konektovan, reconnectDelay će uraditi svoje;
//       // u praksi često želiš queue poruke, ali za MVP ovo je ok.
//     }
//   }
// }

import { Injectable, NgZone, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject } from 'rxjs';
import { environment } from '../../env/enviroment';
import { AdminSendChatMessageDTO } from '../model/admin.send.chat.message';
import { ChatMessageDTO } from '../model/chat.message';
import { SendChatMessageDTO } from '../model/send.chat.message';

@Injectable({ providedIn: 'root' })
export class SupportChatWsService {
  private client?: Client;
  private SockJS?: any; // constructor fn

  private connected$ = new BehaviorSubject<boolean>(false);

  private connectPromise?: Promise<void>;
  private resolveConnect?: () => void;

  constructor(
    private zone: NgZone,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  isConnected() {
    return this.connected$.asObservable();
  }

  // async connect(): Promise<void> {
  //   if (!isPlatformBrowser(this.platformId)) return;
  //   if (this.client && (this.client.connected || this.client.active)) return;

  //   // polyfill pre učitavanja sockjs
  //   (globalThis as any).global ??= globalThis;

  //   // dinamički import (sprečava "global is not defined" pri bundlovanju)
  //   if (!this.SockJS) {
  //     const sockjsMod: any = await import('sockjs-client');
  //     this.SockJS = sockjsMod.default ?? sockjsMod;
  //   }

  //   const socketUrl = `${environment.apiHost}/ws`;

  //   this.client = new Client({
  //     webSocketFactory: () => new this.SockJS(socketUrl),
  //     reconnectDelay: 2000,
  //     debug: () => {},
  //   });

  //   this.client.onConnect = () => {
  //     this.zone.run(() => this.connected$.next(true));
  //   };

  //   this.client.onDisconnect = () => {
  //     this.zone.run(() => this.connected$.next(false));
  //   };

  //   this.client.onStompError = (frame) => {
  //     console.error('STOMP error', frame.headers['message'], frame.body);
  //   };

  //   this.client.activate();
  // }

  async connect(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) return;

    // ako već postoji promise, čekaj ga
    if (this.connectPromise) return this.connectPromise;

    // ako već ima aktivnog klijenta i konektovan je
    if (this.client && this.client.connected) return;

    // napravi promise koji se resolve-uje na onConnect
    this.connectPromise = new Promise<void>((resolve) => {
      this.resolveConnect = resolve;
    });

    (globalThis as any).global ??= globalThis;

    if (!this.SockJS) {
      const sockjsMod: any = await import('sockjs-client');
      this.SockJS = sockjsMod.default ?? sockjsMod;
    }

    const socketUrl = `${environment.apiHost}/ws`;

    this.client = new Client({
      webSocketFactory: () => new this.SockJS(socketUrl),
      reconnectDelay: 2000,
      debug: () => {},
    });

    this.client.onConnect = () => {
      this.zone.run(() => this.connected$.next(true));
      this.resolveConnect?.();
      this.resolveConnect = undefined;
    };

    this.client.onDisconnect = () => {
      this.zone.run(() => this.connected$.next(false));
      // reset promise da sledeći connect može ponovo da čeka
      this.connectPromise = undefined;
      this.resolveConnect = undefined;
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP error', frame.headers['message'], frame.body);
    };

    this.client.activate();

    return this.connectPromise;
  }

  disconnect(): void {
    if (!this.client) return;
    this.client.deactivate();
    this.client = undefined;
    this.connected$.next(false);
  }

  // ✅ pošto connect sad može biti async, uradi "fire and forget"
  private ensureConnected() {
    if (!this.client) {
      void this.connect();
    }
  }

  // subscribeChat(userId: number, onMessage: (m: ChatMessageDTO) => void): StompSubscription {
  //   this.ensureConnected();

  //   // ⚠️ ako pozoveš subscribe PRE nego što se connect završi, može fail.
  //   // Najbrže MVP rešenje: pretpostavi da je connect već pozvan pre subscribe (npr. u component ngOnInit: await ws.connect()).
  //   return this.client!.subscribe(`/topic/chats/users/${userId}`, (msg: IMessage) => {
  //     const parsed = JSON.parse(msg.body) as ChatMessageDTO;
  //     this.zone.run(() => onMessage(parsed));
  //   });
  // }

  // subscribeAll(onMessage: (m: ChatMessageDTO) => void): StompSubscription {
  //   this.ensureConnected();
  //   return this.client!.subscribe(`/topic/chats/users/all`, (msg: IMessage) => {
  //     const parsed = JSON.parse(msg.body) as ChatMessageDTO;
  //     this.zone.run(() => onMessage(parsed));
  //   });
  // }

  async subscribeAll(onMessage: (m: ChatMessageDTO) => void): Promise<StompSubscription> {
    await this.connect();
    if (!this.client?.connected) throw new Error('STOMP not connected yet');
    return this.client!.subscribe(`/topic/chats/users/all`, (msg: IMessage) => {
      const parsed = JSON.parse(msg.body) as ChatMessageDTO;
      this.zone.run(() => onMessage(parsed));
    });
  }

  async subscribeChat(userId: number, onMessage: (m: ChatMessageDTO) => void): Promise<StompSubscription> {
    await this.connect();
    if (!this.client?.connected) throw new Error('STOMP not connected yet');
    return this.client!.subscribe(`/topic/chats/users/${userId}`, (msg: IMessage) => {
      const parsed = JSON.parse(msg.body) as ChatMessageDTO;
      this.zone.run(() => onMessage(parsed));
    });
  }

  sendUserMessage(dto: SendChatMessageDTO): void {
    this.ensureConnected();
    this.client!.publish({
      destination: `/app/chats/send`,
      body: JSON.stringify(dto),
    });
  }

  sendAdminMessage(dto: AdminSendChatMessageDTO): void {
    this.ensureConnected();
    this.client!.publish({
      destination: `/app/admin/chats/send`,
      body: JSON.stringify(dto),
    });
  }
}