import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapComponent } from '../map.component/map.component';
import { PanelComponent } from '../panel.component/panel.component';

@Component({
  selector: 'app-logedin-page',
  standalone: true,
  imports: [CommonModule, MapComponent, PanelComponent],
  templateUrl: './logedin.page.component.html',
  styleUrls: ['./logedin.page.component.css'],
})
export class LogedinPageComponent {
  panelOpen = false;

  toastMessage = '';
  private toastTimer: any;

  constructor(private cdr: ChangeDetectorRef) {}

  togglePanel() {
    this.panelOpen = !this.panelOpen;

    setTimeout(() => {
      window.dispatchEvent(new Event('resize'));
    }, 300);
  }

  showToast(msg: string) {
    this.toastMessage = msg;
    this.cdr.detectChanges();

    if (this.toastTimer) clearTimeout(this.toastTimer);

    this.toastTimer = setTimeout(() => {
      this.toastMessage = '';
      this.cdr.detectChanges();
    }, 2000);
  }
}
