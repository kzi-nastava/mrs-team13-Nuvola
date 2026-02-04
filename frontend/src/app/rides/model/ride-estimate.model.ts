export interface RideEstimateRequestDTO {
  startAddress: string;
  destinationAddress: string;
}

export interface RideEstimateResponseDTO {
  startAddress: string;
  destinationAddress: string;
  estimatedTimeMinutes: number;
}
