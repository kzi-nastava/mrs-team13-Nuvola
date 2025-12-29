package Nuvola.Projekatsiit2025.dto;

public class RideEstimateResponseDTO {
    private String startAddress;
    private String destinationAddress;
    private int estimatedTimeMinutes;

    public RideEstimateResponseDTO() {}

    public RideEstimateResponseDTO(String startAddress, String destinationAddress, int estimatedTimeMinutes) {
        this.startAddress = startAddress;
        this.destinationAddress = destinationAddress;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public int getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public void setEstimatedTimeMinutes(int estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }
}
