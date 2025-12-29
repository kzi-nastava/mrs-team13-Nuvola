package Nuvola.Projekatsiit2025.dto;

public class RideEstimateRequestDTO {
    
    private String startAddress;
    private String destinationAddress;

    public RideEstimateRequestDTO() {}


    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
}
