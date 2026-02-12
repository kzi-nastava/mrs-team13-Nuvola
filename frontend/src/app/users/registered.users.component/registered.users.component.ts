import { Component, OnInit } from '@angular/core';
import { UserModel } from '../model/user.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminUserService } from '../admin-user.service';

@Component({
  selector: 'app-registeredusers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './registered.users.component.html',
  styleUrl: './registered.users.component.css',
})
export class RegisteredUsersComponent implements OnInit {

  users: UserModel[] = [];
  selectedUser: UserModel | null = null;
  blockReason: string = '';
  showModal = false;

  constructor(private adminUserService: AdminUserService) {}

  ngOnInit() {
    this.adminUserService.getRegisteredUsers().subscribe(data => {
      this.users = data;
    });
  }

  openBlockModal(user: UserModel) {
    this.selectedUser = user;
    this.blockReason = '';
    this.showModal = true;
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