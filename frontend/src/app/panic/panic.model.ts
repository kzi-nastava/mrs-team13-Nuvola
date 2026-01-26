export type PanicTriggeredBy = 'DRIVER' | 'PASSENGER';

export interface PanicNotification {
  panicId: number;       
  rideId: number;
  triggeredBy: PanicTriggeredBy;
  time: string;           
  location?: { lat: number; lng: number };
  vehicleId?: number;
  seen?: boolean;          
}
