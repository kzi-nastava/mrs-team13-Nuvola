package Nuvola.Projekatsiit2025.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RideReportDayDTO {
    private String date;
    private long rideCount;
    private double totalKm;
    private double totalMoney;
}
