type RideDTO = {
  id: number;
  from: { latitude: number; longitude: number };
  to: { latitude: number; longitude: number };
  stops: Array<{ id: number; name?: string; latitude: number; longitude: number; order?: number }>;
  vehicleId: number;
};