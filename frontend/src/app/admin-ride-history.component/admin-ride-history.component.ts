import { Component, OnInit } from '@angular/core';
import { AdminRideService } from '../services/admin-ride.service';
import { AdminRideHistory, AdminRideResponse, AdminRideDetails } from '../admin-ride-history.component/model/admin-ride-history.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RideDetailsComponent } from '../ride-details-admin.component/ride-details-admin.component';

@Component({
  selector: 'app-ride-history-admin',
  templateUrl: './admin-ride-history.component.html',
  styleUrls: ['./admin-ride-history.component.css'],
  imports: [CommonModule, FormsModule, RideDetailsComponent],
  standalone:true
})
export class RideHistoryAdminComponent implements OnInit {
  // Podaci o voznjama
  rides: AdminRideHistory[] = [];
  totalPages: number = 0;
  currentPage: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;

  // Parametri za filtriranje i sortiranje
  sortBy: string = 'startTime';
  sortDir: string = 'DESC';
  filterDate: string = '';

  // Za detaljni prikaz
  selectedRide: AdminRideDetails | null = null;
  showDetailsModal: boolean = false;

  // Učitavanje
  isLoading: boolean = false;
  errorMessage: string = '';

  // User ID (trebao bi iz sesije/routera)
  userId: string = 'user-id-here';

  constructor(private adminRideService: AdminRideService) {}

  ngOnInit(): void {
    this.loadRides();
  }

  /**
   * Učitaj voznje sa API-ja
   */
  loadRides(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.adminRideService.getRideHistory(
      this.userId,
      this.currentPage,
      this.pageSize,
      this.sortBy,
      this.sortDir,
      this.filterDate
    ).subscribe({
      next: (response: AdminRideResponse) => {
        this.rides = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Greška pri učitavanju voznji!';
        console.error(error);
        this.isLoading = false;
      }
    });
  }

  /**
   * Promijeni sortianje
   */
  changeSortBy(field: string): void {
    if (this.sortBy === field) {
      // Ako je isti field, promijeni smjer
      this.sortDir = this.sortDir === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortBy = field;
      this.sortDir = 'DESC';
    }
    this.currentPage = 0;
    this.loadRides();
  }

  /**
   * Filtriraj po datumu
   */
  filterByDate(): void {
    this.currentPage = 0;
    this.loadRides();
  }

  /**
   * Očisti filter
   */
  clearFilter(): void {
    this.filterDate = '';
    this.currentPage = 0;
    this.loadRides();
  }

  /**
   * Idi na stranicu
   */
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadRides();
    }
  }

  /**
   * Otvori detaljni prikaz voznje
   */
  openRideDetails(rideId: number): void {
    this.isLoading = true;

    this.adminRideService.getRideDetails(rideId).subscribe({
      next: (ride: AdminRideDetails) => {
        this.selectedRide = ride;
        this.showDetailsModal = true;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Greška pri učitavanju detalja voznje!';
        console.error(error);
        this.isLoading = false;
      }
    });
  }

  /**
   * Zatvori detaljni prikaz
   */
  closeDetailsModal(): void {
    this.showDetailsModal = false;
    this.selectedRide = null;
  }

  /**
   * Formatiraj vrijeme
   */
  formatTime(dateString: string): string {
    return new Date(dateString).toLocaleString('sr-RS');
  }

  /**
   * Prikaži status otkazivanja
   */
  getCancelStatus(ride: AdminRideHistory): string {
    if (ride.rideStatus === 'CANCELLED') {
      return ride.cancelledBy ? `Otkazano od ${ride.cancelledBy}` : 'Otkazano';
    }
    return 'Završeno';
  }

  /**
   * Prikaži ikonicu za PANIC
   */
  isPanic(ride: AdminRideHistory): boolean {
    return ride.panic;
  }
}
