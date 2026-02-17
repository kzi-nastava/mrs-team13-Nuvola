export type RideStatus = 'FINISHED' | 'SCHEDULED' | 'IN_PROGRESS' | 'CANCELED';

export interface RegisteredUserRideHistoryItemDTO {
  id: number;
  pickup: string;
  dropoff: string;
  startTime: string | null;
  endTime: string | null;
  creationTime: string;
  price: number;
  status: RideStatus;
  favorite?: boolean;
  driver: DriverInfoDTO;
  routeId:number;
}

export interface DriverInfoDTO {  id: number;  firstName: string;  lastName: string;  phone: string;  email: string;  status: string;  vehicleType: string;}

export interface RatingInfoDTO {
  id: number;
  score: number;
  comment?: string;
  createdAt?: string;
}

export interface RideHistoryDetailsDTO {
  id: number;
  pickup: string; // "lat,lng"
  dropoff: string; // "lat,lng"
  startTime: string | null;
  endTime: string | null;
  creationTime: string;
  price: number;
  status: RideStatus;
  driver?: DriverInfoDTO;
  ratings?: RatingInfoDTO[];
  reports?: Array<{
    id: number;
    reason: string;
    createdAt?: string;
  }>;
  reorderTemplate?: any;
  routeId: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}
