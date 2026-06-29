package dto;

import java.util.ArrayList;
import java.util.List;

public class RideReportResponse {

    private List<RideReportDayDTO> data =
            new ArrayList<>();

    private long totalRides;

    private double totalKm;
    private double totalMoney;

    private double avgRides;
    private double avgKm;
    private double avgMoney;

    public RideReportResponse() {
    }

    public List<RideReportDayDTO> getData() {
        return data;
    }

    public void setData(List<RideReportDayDTO> data) {
        this.data = data == null
                ? new ArrayList<>()
                : data;
    }

    public long getTotalRides() {
        return totalRides;
    }

    public void setTotalRides(long totalRides) {
        this.totalRides = totalRides;
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

    public double getAvgRides() {
        return avgRides;
    }

    public void setAvgRides(double avgRides) {
        this.avgRides = avgRides;
    }

    public double getAvgKm() {
        return avgKm;
    }

    public void setAvgKm(double avgKm) {
        this.avgKm = avgKm;
    }

    public double getAvgMoney() {
        return avgMoney;
    }

    public void setAvgMoney(double avgMoney) {
        this.avgMoney = avgMoney;
    }
}