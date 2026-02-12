import { Component, OnInit } from '@angular/core';
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

  constructor(private adminUserService: AdminUserService) {}

  ngOnInit() {
    this.adminUserService.getDrivers().subscribe(data => {
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

    // SAMO UI SIMULACIJA
    this.selectedUser.isBlocked = true;
    this.selectedUser.blockingReason = this.blockReason;

    this.showModal = false;
  }
}