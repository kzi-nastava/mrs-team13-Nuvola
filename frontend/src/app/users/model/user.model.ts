export interface UserModel {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  address: string;
  phone: string;
  picture: string;
  isBlocked: boolean;
  blockingReason: string;
}
