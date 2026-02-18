import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { UserModel } from '../model/user.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminUserService } from '../admin-user.service';

@Component({
  selector: 'app-drivers',
    standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './drivers.component.html',
  styleUrl: './drivers.component.css',
})
export class DriversComponent implements OnInit {

users: UserModel[] = [];
  selectedUser: UserModel | null = null;
  blockReason: string = '';
  showModal = false;
  searchTerm = '';

  constructor(private adminUserService: AdminUserService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadDrivers();   
  }

  loadDrivers(searchTerm?: string) {    
    if (searchTerm) {
      searchTerm = searchTerm.trim();
      if (searchTerm === '') {
        this.loadDrivers();
        return;
      }
      this.adminUserService.getDrivers(searchTerm).subscribe(data => {
        this.users = data;
        this.cdr.detectChanges();
      });

    } else {
      this.adminUserService.getDrivers().subscribe(data => {
        this.users = data;
        this.cdr.detectChanges();
      });
    }
    
  }

  openBlockModal(user: UserModel) {
    this.selectedUser = user;
    this.blockReason = '';
    this.showModal = true;
  }

  openInfo(user: UserModel) {
    console.log('Driver info:', user);

    
  }

  cancel() {
    this.showModal = false;
  }

  getImageUrl(picture: string | null): string {
  if (!picture) {
    return "/images/default-user.png";
  }

  return 'http://localhost:8080/api/profile/picture/' + picture;
}


submitBlock() {
  if (!this.selectedUser) return;

  const reason = this.blockReason?.trim() || null;

  this.adminUserService
    .blockUser(this.selectedUser.id, reason)
    .subscribe(updatedUser => {

      this.selectedUser!.blocked = updatedUser.blocked;
      this.selectedUser!.blockingReason = updatedUser.blockingReason;

      this.showModal = false;
    });
}

showUnblockModal = false;

openUnblockModal(user: UserModel) {
  this.selectedUser = user;
  this.showUnblockModal = true;
}

confirmUnblock() {
  if (!this.selectedUser) return;

  this.adminUserService
    .unblockUser(this.selectedUser.id)
    .subscribe(updatedUser => {

      this.selectedUser!.blocked = updatedUser.blocked;
      this.selectedUser!.blockingReason = null;

      this.showUnblockModal = false;
    });
}


}