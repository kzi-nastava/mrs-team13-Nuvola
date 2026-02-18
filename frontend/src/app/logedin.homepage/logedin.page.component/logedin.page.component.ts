import { Component, ChangeDetectorRef,OnInit, OnDestroy} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MapComponent } from '../map.component/map.component';
import { PanelComponent } from '../panel.component/panel.component';
import { LocationModel } from '../models/location.model';
import { FavoriteApiService } from '../services/favorite-api.service';

type FavoriteRoute = {
  id: number;
  from: LocationModel;
  to: LocationModel;
  stops: LocationModel[];
};

type FavoriteRouteDTO = {
  id: number;
  startLocation: string; // "lat, lng" format
  destination: string;
  stops: string[];
};

@Component({
  selector: 'app-logedin-page',
  standalone: true,
  imports: [CommonModule, MapComponent, PanelComponent],
  templateUrl: './logedin.page.component.html',
  styleUrls: ['./logedin.page.component.css'],
})

export class LogedinPageComponent implements OnInit, OnDestroy {
  panelOpen = false;

  // toastMessage = '';
  // private toastTimer: any;


  favoritesOpen = false;
  selectedFavoriteRoute: FavoriteRoute | null = null;
  favoriteRoutes: FavoriteRoute[] = [];
  favoritesLoading = false;
  favoritesError = false;

  constructor(
    private cdr: ChangeDetectorRef,
    private favoriteApi: FavoriteApiService
  ) {}

  ngOnInit(): void {
    this.loadFavorites();
  }

loadFavorites(): void {
  this.favoritesError = false;
  console.log('=== loadFavorites() called');

  this.favoriteApi.getFavorites().subscribe({
    next: (data: any[]) => {
      console.log('=== Favorites data received:', data);
      console.log('=== Count:', data.length);
      this.favoriteRoutes = data.map(r => this.mapToFavoriteRoute(r));
      console.log('=== Mapped routes:', this.favoriteRoutes);
    },
    error: (err) => {
      console.error('=== Favorites failed:', err);
      this.favoritesError = true;
      this.favoriteRoutes = [];
    }
  });
}

  private mapToFavoriteRoute(r: any): FavoriteRoute {
    return {
      id: r.id,
      from: this.parseLocation(r.startLocation),
      to: this.parseLocation(r.destination),
      stops: (r.stops ?? []).map((s: string) => this.parseLocation(s)),
    };
  }

   private parseLocation(value: string): LocationModel {
    if (!value) return { address: '', lat: 0, lng: 0 };

    // Pokušaj da parsiraš "lat, lng" format
    const parts = value.split(',').map(p => p.trim());
    if (parts.length === 2 && !isNaN(Number(parts[0])) && !isNaN(Number(parts[1]))) {
      return {
        address: value, // koristimo koordinate kao adresu dok backend ne vrati pravu adresu
        lat: Number(parts[0]),
        lng: Number(parts[1]),
      };
    }

    // Ako je već adresa (string koji nije koordinate)
    return { address: value, lat: 0, lng: 0 };
  }


  togglePanel() {
    this.panelOpen = !this.panelOpen;

    setTimeout(() => {
      window.dispatchEvent(new Event('resize'));
    }, 300);
  }

//   showToast(msg: string) {
//   this.toastMessage = msg;
//   this.cdr.detectChanges();

//   if (this.toastTimer) clearTimeout(this.toastTimer);

//   this.toastTimer = setTimeout(() => {
//     this.toastMessage = '';
//     this.cdr.detectChanges(); 
//   }, 2000);
// }

  openFavoritesPopup() {
    this.loadFavorites();
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

    //this.showToast('Route loaded from favorites.');
  }

  ngOnDestroy(): void {
  //if (this.toastTimer) clearTimeout(this.toastTimer);
}

}