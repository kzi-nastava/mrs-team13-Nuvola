package dto;

public class RideReportDayDTO {

    private String date;
    private long rideCount;
    private double totalKm;
    private double totalMoney;

    public RideReportDayDTO() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getRideCount() {
        return rideCount;
    }

    public void setRideCount(long rideCount) {
        this.rideCount = rideCount;
    }

    public double getTotalKm() {
        return totalKm;
    }

    public void setTotalKm(double totalKm) {
        this.totalKm = totalKm;
    }

    public double getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(double totalMoney) {
        this.totalMoney = totalMoney;
    }
}