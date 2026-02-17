import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../env/enviroment';
import { FormsModule } from '@angular/forms';

type RequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

type ChangeRequest = {
  id: number;
  driverName: string;
  driverEmail: string;
  
  // Current values
  currentFirstName: string;
  currentLastName: string;  
  currentPhone: string;
  currentAddress: string;
  currentModel: string;
  currentType: string;
  currentNumOfSeats: number;
  currentBabyFriendly: boolean;
  currentPetFriendly: boolean;
  
  // Requested values
  firstName: string;
  lastName: string;
  phone: string;
  address: string;
  model: string;
  type: string;
  numOfSeats: number;
  babyFriendly: boolean;
  petFriendly: boolean;
  
  status: RequestStatus;
  createdAt: string;
};

@Component({
  selector: 'app-profile-change-request',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.change.request.component.html',
  styleUrl: './profile.change.request.component.css',
})
export class ProfileChangeRequestComponent implements OnInit {
  requests: ChangeRequest[] = [];
  selectedRequest: ChangeRequest | null = null;
  showApproveModal = false;
  showRejectModal = false;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadRequests();
  }

  loadRequests() {
    this.http.get<any[]>(`${environment.apiHost}/api/admin/profile-change-requests`)
      .subscribe({
        next: (data) => {
          console.log('Requests loaded:', data);
          this.requests = data;
        },
        error: (err) => {
          console.error('Error loading requests:', err);
        }
      });
  }

  openApproveModal(request: ChangeRequest) {
    this.selectedRequest = request;
    this.showApproveModal = true;
  }

  openRejectModal(request: ChangeRequest) {
    this.selectedRequest = request;
    this.showRejectModal = true;
  }
  
  hasChanged(current: any, requested: any): boolean {
    return current !== requested;
  }

  approveRequest() {
    if (!this.selectedRequest) return;

    this.http.put(
      `${environment.apiHost}/api/admin/profile-change-requests/${this.selectedRequest.id}/approve`,
      {}
    ).subscribe({
      next: () => {
        this.showApproveModal = false;
        this.loadRequests();
      },
      error: (err) => {
        console.error('Error approving request:', err);
      }
    });
  }

  rejectRequest() {
    if (!this.selectedRequest) return;

    this.http.put(
      `${environment.apiHost}/api/admin/profile-change-requests/${this.selectedRequest.id}/reject`,
      {}
    ).subscribe({
      next: () => {
        this.showRejectModal = false;
        this.loadRequests(); // Refresh lista
      },
      error: (err) => {
        console.error('Error rejecting request:', err);
      }
    });
  }
}