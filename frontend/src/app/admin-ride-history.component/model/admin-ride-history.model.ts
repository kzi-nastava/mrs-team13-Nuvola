export interface AdminRideHistory {
  id: number;
  driverName: string;
  startLocation: string;
  destination: string;
  startTime: string;
  endTime: string;
  creationDate: string;
  distance?: number;
  price: number;
  rideStatus: string;
  cancelledBy?: string; // DRIVER, PASSENGER, null
  panic: boolean;
}

export interface AdminRideDetails {
  id: number;
  startLocation: string;
  destination: string;
  startTime: string;
  endTime: string;
  creationDate: string;
  price: number;
  panic: boolean;
  routeCoordinates: string[]; // ["45.2671,19.8335", "45.255,19.845"]
  driverName: string;
  passengerNames: string[];
  inconsistencyReports: any[];
  driverRating: number | null;
  passengersRating: number | null;
  canReorderNow: boolean;
  canReorderLater: boolean;
}

export interface AdminRideResponse {
  content: AdminRideHistory[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
}
