package dto;
import java.util.ArrayList;
import java.util.List;
public class CreateRideFromFavoriteDTO {
    public List<String> passengerEmails = new ArrayList<>();
    public String vehicleType;
    public boolean babyTransport, petTransport;
    public String scheduledTime;
}
