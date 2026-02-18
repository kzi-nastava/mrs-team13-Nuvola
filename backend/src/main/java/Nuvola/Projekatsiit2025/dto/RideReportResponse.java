package Nuvola.Projekatsiit2025.dto;
import lombok.Data;
import java.util.List;

@Data
public class RideReportResponse {
    private List<RideReportDayDTO> data;
    private long totalRides;
    private double totalKm;
    private double totalMoney;
    private double avgRides;
    private double avgKm;
    private double avgMoney;
}
