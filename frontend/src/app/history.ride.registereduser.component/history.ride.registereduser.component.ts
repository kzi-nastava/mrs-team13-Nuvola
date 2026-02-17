import { Component, OnInit } from '@angular/core';
import { RideHistoryService } from '../homepage/service/ride-history.service';
import { PageResponse, RegisteredUserRideHistoryItemDTO } 
  from './model/ride-history.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RideHistoryDetailsComponent } from '../ride-history-details.component/ride-history-details.component';

@Component({
  selector: 'app-ride-history',
  standalone: true,
  imports: [CommonModule, FormsModule, RideHistoryDetailsComponent], //fix proverirti
  templateUrl: './history.ride.registereduser.component.html',
  styleUrls: ['./history.ride.registereduser.component.css'],
})
export class RideHistoryComponent implements OnInit {
  pageData?: PageResponse<RegisteredUserRideHistoryItemDTO>;
  loading = false;
  error?: string;

  // UI state
  sortBy: string = 'startTime';
  sortOrder: 'asc' | 'desc' = 'desc';
  fromDate: string = ''; // yyyy-MM-dd
  toDate: string = '';
  page = 0;
  size = 20;

  constructor(private rideHistory: RideHistoryService) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    console.log('RideHistory load()', {
  sortBy: this.sortBy, sortOrder: this.sortOrder, fromDate: this.fromDate, toDate: this.toDate, page: this.page, size: this.size
});

    this.loading = true;
    this.error = undefined;

    this.rideHistory.getHistory({
      sortBy: this.sortBy,
      sortOrder: this.sortOrder,
      fromDate: this.fromDate || undefined,
      toDate: this.toDate || undefined,
      page: this.page,
      size: this.size
    }).subscribe({
      next: (res) => {
        this.pageData = res;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || err?.error?.error || 'Greška pri učitavanju istorije vožnji.';
      }
    });
  }

  applyFilters() {
    this.page = 0;
    this.load();
  }

  nextPage() {
    if (!this.pageData?.last) {
      this.page++;
      this.load();
    }
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.load();
    }
  }

  toggleSort(order?: 'asc'|'desc') {
    this.sortOrder = order ?? (this.sortOrder === 'asc' ? 'desc' : 'asc');
    this.load();
  }

  toggleFav(r: RegisteredUserRideHistoryItemDTO) {
  const prev = !!r.favorite;

  // optimistic UI (odmah prebacimo srce)
  r.favorite = !prev;

  this.rideHistory.toggleFavorite(r.id).subscribe({
    next: (res) => {
      r.favorite = res.favourite;
    },
    error: () => {
      // rollback ako pukne request
      r.favorite = prev;
      this.error = 'Ne mogu da sačuvam omiljenu rutu. Pokušaj ponovo.';
    }
  });
  
}
selectedRideId?: number;

openDetails(id: number) {
  this.selectedRideId = id;
}

closeDetails() {
  this.selectedRideId = undefined;
}

}
