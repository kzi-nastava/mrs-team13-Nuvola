import { Component, ChangeDetectorRef, OnDestroy} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapComponent } from '../map.component/map.component';
import { PanelComponent } from '../panel.component/panel.component';
import { LocationModel } from '../models/location.model';

type FavoriteRoute = {
  id: number;
  from: LocationModel;
  to: LocationModel;
  stops: LocationModel[];
};

@Component({
  selector: 'app-logedin-page',
  standalone: true,
  imports: [CommonModule, MapComponent, PanelComponent],
  templateUrl: './logedin.page.component.html',
  styleUrls: ['./logedin.page.component.css'],
})

export class LogedinPageComponent implements OnDestroy {
  panelOpen = false;

  toastMessage = '';
  private toastTimer: any;

  constructor(private cdr: ChangeDetectorRef) {}


  favoritesOpen = false;

  selectedFavoriteRoute: FavoriteRoute | null = null;

  favoriteRoutes: FavoriteRoute[] = [
  {
    id: 1,
    from: { address: 'Bulevar Oslobođenja 30', lat: 45.257, lng: 19.845 },
    to: { address: 'Železnička 40', lat: 45.267, lng: 19.833 },
    stops: [{ address: 'Kisačka 12', lat: 45.271, lng: 19.833 }],
  },
  {
    id: 2,
    from: { address: 'Narodnog fronta 10', lat: 45.244, lng: 19.842 },
    to: { address: 'Bulevar Mihajla Pupina 1', lat: 45.2555, lng: 19.8445 },
    stops: [],
  },

  {
    id: 3,
    from: { address: 'Cara Dušana 5', lat: 45.252, lng: 19.836 },
    to: { address: 'Bulevar patrijarha Pavla 12', lat: 45.264, lng: 19.804 },
    stops: [],
  },

  {
    id: 4,
    from: { address: 'Jevrejska 18', lat: 45.255, lng: 19.843 },
    to: { address: 'Futoška 109', lat: 45.259, lng: 19.816 },
    stops: [{ address: 'Bulevar cara Lazara 79', lat: 45.2456, lng: 19.8401 }],

  },

  {
    id: 5,
    from: { address: 'Bulevar cara Lazara 92', lat: 45.239, lng: 19.845 },
    to: { address: 'Temerinska 101', lat: 45.287, lng: 19.845 },
    stops: [
      { address: 'Bulevar Evrope 25', lat: 45.266, lng: 19.801 },
      { address: 'Rumenacka 35', lat: 45.269, lng: 19.790 },
    ],
  },
];


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

  openFavoritesPopup() {
    this.favoritesOpen = true;
  }

  closeFavoritesPopup() {
    this.favoritesOpen = false;
  }

  orderFromFavorite(route: FavoriteRoute) {
    this.selectedFavoriteRoute = {
  ...route,
  stops: [...route.stops]
};
    this.favoritesOpen = false;

    this.showToast('Route loaded from favorites.');
  }

  removeFromFavorites(routeId: number) {
    this.favoriteRoutes = this.favoriteRoutes.filter(r => r.id !== routeId);

    if (this.selectedFavoriteRoute?.id === routeId) {
      this.selectedFavoriteRoute = null;
    }
  }
  ngOnDestroy(): void {
  if (this.toastTimer) clearTimeout(this.toastTimer);
}

}