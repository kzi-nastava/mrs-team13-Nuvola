import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserModel } from './model/user.model';
import { environment } from '../env/enviroment';

@Injectable({
  providedIn: 'root'
})
export class AdminUserService {

  constructor(private http: HttpClient) {}

  getRegisteredUsers(): Observable<UserModel[]> {
  return this.http.get<UserModel[]>(
    environment.apiHost + '/api/admin/users/registeredUsers'
  );
}

getDrivers(): Observable<UserModel[]> {
  return this.http.get<UserModel[]>(
    environment.apiHost + '/api/admin/users/drivers'
  );
}

blockUser(userId: number, reason: string | null) {
  return this.http.post<any>(
    environment.apiHost + `/api/admin/users/${userId}/block`,
    { blockingReason: reason }
  );
}

unblockUser(userId: number) {
  return this.http.post<any>(
    environment.apiHost + `/api/admin/users/${userId}/unblock`,
    {}
  );
}

}
