import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { VehiclesMap } from '../vehicles.map/vehicles.map';
import { RoutePanel } from '../route.panel/route.panel';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-homepage.component',
  imports: [VehiclesMap, RoutePanel, CommonModule],
  templateUrl: './homepage.component.html',
  styleUrl: './homepage.component.css',
})
export class HomepageComponent implements OnDestroy {
  panelOpen = false;

  toastMessage = '';
  private toastTimer: any;

  constructor(private cdr: ChangeDetectorRef) {}

  togglePanel() {
    this.panelOpen = !this.panelOpen;

    // setTimeout(() => {
    //   window.dispatchEvent(new Event('resize'));
    // }, 300);
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

  
  ngOnDestroy(): void {
  if (this.toastTimer) clearTimeout(this.toastTimer);
}

onCleared() {
    
    console.log('Route cleared');
  }

}
