import { Injectable, NgZone } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Subscription, interval } from 'rxjs';
import { environment } from '../env/enviroment';

type PositionUpdateRequestDTO = {
  driverId: number;
  latitude: number;
  longitude: number;
};

@Injectable({ providedIn: 'root' })
export class DriverLocationPublisherService {
  private stompClient?: Client;
  private sendSub?: Subscription;

  private running$ = new BehaviorSubject<boolean>(false);
  private driverId: number | null = null;

  private watchId: number | null = null; // ako koristiš watchPosition

  constructor(private zone: NgZone) {}

  /** Start sending location updates */
  start(driverId: number) {
    this.driverId = driverId;
    if (this.running$.value) return;

    this.running$.next(true);
    this.connectWs().catch(console.error);
  }

  /** Stop sending location updates */
  stop() {
    this.running$.next(false);

    this.sendSub?.unsubscribe();
    this.sendSub = undefined;

    if (this.watchId !== null) {
      try { navigator.geolocation.clearWatch(this.watchId); } catch {}
      this.watchId = null;
    }

    this.driverId = null;

    try { this.stompClient?.deactivate(); } catch {}
    this.stompClient = undefined;
  }

  // private connectWs() {
  //   if (!this.driverId) return;

  //   if (this.stompClient?.active) return;

  //   this.stompClient = new Client({
  //     webSocketFactory: () => new SockJS(environment.apiHost + '/ws'),
  //     reconnectDelay: 3000,
  //     heartbeatIncoming: 10000,
  //     heartbeatOutgoing: 10000,
  //     debug: () => {},
  //     onConnect: () => {
  //       this.startSending();
  //     },
  //     onWebSocketClose: () => {
  //       // if service still needs to run, STOMP will attempt reconnect due to reconnectDelay
  //     },
  //     onStompError: (frame) => console.error('STOMP error', frame),
  //   });

  //   this.stompClient.activate();
  // }

  private async connectWs() {
    if (!this.driverId) return;
    if (this.stompClient?.active) return;

    // polyfill za biblioteke koje očekuju Node "global"
    (globalThis as any).global ??= globalThis;

    const sockjsMod: any = await import('sockjs-client');
    const SockJS = sockjsMod.default ?? sockjsMod;

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(environment.apiHost + '/ws'),
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => {},
      onConnect: () => this.startSending(),
      onWebSocketClose: () => {},
      onStompError: (frame) => console.error('STOMP error', frame),
    });

    this.stompClient.activate();
  }

  private startSending() {
    // If the service no longer needs to run, do not send
    if (!this.running$.value || !this.driverId) return;

    // Option A (recommended): watchPosition (sends when there are changes, better for battery)
    this.startWatchPosition();

    // Option B: interval polling
    // this.startIntervalSending();
  }

  private startWatchPosition() {
    if (!navigator.geolocation) {
      console.warn('Geolocation not supported');
      return;
    }

    // Do not start twice
    if (this.watchId !== null) return;

    // Important: geolocation callback may run outside Angular zone
    this.zone.runOutsideAngular(() => {
      this.watchId = navigator.geolocation.watchPosition(
        (pos) => {
          if (!this.running$.value || !this.driverId) return;

          const payload: PositionUpdateRequestDTO = {
            driverId: this.driverId,
            latitude: pos.coords.latitude,
            longitude: pos.coords.longitude,
          };

          this.stompClient?.publish({
            destination: '/app/vehicle/position', // matches @MessageMapping("/vehicle/position")
            body: JSON.stringify(payload),
          });
        },
        (err) => console.warn('watchPosition error', err),
        { enableHighAccuracy: true, maximumAge: 1000, timeout: 8000 }
      );
    });
  }

  private startIntervalSending() {
    this.sendSub?.unsubscribe();

    this.sendSub = interval(2000).subscribe(async () => {
      if (!this.running$.value || !this.driverId) return;

      try {
        const { lat, lng } = await this.getCurrentLocationOnce();

        const payload: PositionUpdateRequestDTO = {
          driverId: this.driverId,
          latitude: lat,
          longitude: lng,
        };

        this.stompClient?.publish({
          destination: '/app/vehicle/position',
          body: JSON.stringify(payload),
        });
      } catch (e) {
        console.warn('Could not read/send location', e);
      }
    });
  }

  private getCurrentLocationOnce(): Promise<{ lat: number; lng: number }> {
    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        (pos) => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
        (err) => reject(err),
        { enableHighAccuracy: true, maximumAge: 1000, timeout: 8000 }
      );
    });
  }
}