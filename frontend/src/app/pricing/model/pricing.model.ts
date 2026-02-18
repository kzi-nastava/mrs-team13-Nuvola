export type VehicleType = 'STANDARD' | 'LUXURY' | 'VAN';

export interface VehicleTypePricingDTO {
  vehicleType: VehicleType;
  basePrice: string;
}

export interface UpdateVehicleTypePriceDTO {
  basePrice: string;
}