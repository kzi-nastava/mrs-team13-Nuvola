import { Component, Input, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { AdminRideDetails } from '../admin-ride-history.component/model/admin-ride-history.model';
import * as L from 'leaflet';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ride-details',
  templateUrl: './ride-details-admin.component.html',
  styleUrls: ['./ride-details-admin.component.css'],
  imports:[CommonModule],
  standalone: true
})
export class RideDetailsComponent implements AfterViewInit {
  @Input() ride: AdminRideDetails | null = null;
  @ViewChild('mapContainer') mapContainer: any;

  map: L.Map | null = null;

  ngAfterViewInit(): void {
    if (this.ride && this.mapContainer) {
      this.initializeMap();
      this.addRouteToMap();
    }
  }

  
  private initializeMap(): void {
    if (!this.mapContainer) return;

    this.map = L.map(this.mapContainer.nativeElement).setView([45.255, 19.845], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(this.map);
  }

  
  private addRouteToMap(): void {
    if (!this.map || !this.ride || !this.ride.routeCoordinates) return;

    const coordinates: L.LatLngExpression[] = this.ride.routeCoordinates.map(coord => {
      const [lat, lng] = coord.split(',').map(c => parseFloat(c));
      return [lat, lng];
    });

    if (coordinates.length > 0) {
    
      L.marker(coordinates[0], {
        icon: L.icon({
          iconUrl: 'assets/marker-green.png',
          shadowUrl: 'assets/marker-shadow.png',
          iconSize: [25, 41],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34]
        })
      })
        .addTo(this.map!)
        .bindPopup('Pickup lokacija');

      if (coordinates.length > 1) {
        L.marker(coordinates[coordinates.length - 1], {
          icon: L.icon({
            iconUrl: 'assets/marker-red.png',
            shadowUrl: 'assets/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34]
          })
        })
          .addTo(this.map!)
          .bindPopup('Dropoff lokacija');
      }

      
      L.polyline(coordinates, {
        color: '#3498db',
        weight: 4,
        opacity: 0.7,
        dashArray: '5, 5'
      }).addTo(this.map!);

     
      const bounds = L.latLngBounds(coordinates);
      this.map!.fitBounds(bounds, { padding: [50, 50] });
    }
  }

  
  formatTime(dateString: string | null | undefined): string { 
       if (!dateString) return 'N/A'; 
          const date = new Date(dateString);
              return date.toLocaleString('sr-RS', { 
                    year: 'numeric',  
                    month: '2-digit',     
                    day: '2-digit',      
                    hour: '2-digit',      
                    minute: '2-digit'    });  
                  }

  
  getRating(rating: number | null | undefined): string {  
    if (rating === null || rating === undefined) return '❌ Nema ocene';   
   return `⭐ ${rating}/5`;  }

  
  getReorderOptions(): string[] {
    const options: string[] = [];
    if (this.ride?.canReorderNow) options.push('Odmah');
    if (this.ride?.canReorderLater) options.push('Kasnije');
    return options.length > 0 ? options : ['Nije dostupno'];
  }
  getDuration(): string {  
      if (!this.ride) return 'N/A';
      try {
          const start = new Date(this.ride.startTime).getTime();
          const end = new Date(this.ride.endTime).getTime();     
          const minutes = Math.floor((end - start) / 60000);      
          return `${minutes} minuta`;    
        } 
          catch {
                  return 'N/A';    
                }  
              }
  isPanic(): boolean {    
    return this.ride?.panic ?? false; 
   }

  getPassengers(): string[] {  
      return this.ride?.passengerNames ?? [];
      }
  getReports(): any[] {   
     return this.ride?.inconsistencyReports ?? [];
      }
}
