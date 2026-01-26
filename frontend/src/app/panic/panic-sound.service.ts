import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class PanicSoundService {
  private enabled = false;
  private audio: HTMLAudioElement | null = null;

  enable() {
    
    this.audio = new Audio('/assets/panic.wav');
    this.enabled = true;

    
    this.audio.volume = 0.8;
    this.audio.play().then(() => {
      this.audio?.pause();
      if (this.audio) this.audio.currentTime = 0;
    }).catch(() => {
      
    });
  }

  play() {
    if (!this.enabled) return;
    if (!this.audio) this.audio = new Audio('/assets/panic.mp3');

    this.audio.currentTime = 0;
    this.audio.play().catch(() => {});
  }

  isEnabled() {
    return this.enabled;
  }
}
