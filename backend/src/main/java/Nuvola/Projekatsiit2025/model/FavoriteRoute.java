package Nuvola.Projekatsiit2025.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class FavoriteRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private RegisteredUser user;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="title", column=@Column(name="from_title")),
            @AttributeOverride(name="latitude", column=@Column(name="from_lat")),
            @AttributeOverride(name="longitude", column=@Column(name="from_lng")),
            @AttributeOverride(name="address", column=@Column(name="from_address"))
    })
    private Location from;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="title", column=@Column(name="to_title")),
            @AttributeOverride(name="latitude", column=@Column(name="to_lat")),
            @AttributeOverride(name="longitude", column=@Column(name="to_lng")),
            @AttributeOverride(name="address", column=@Column(name="to_address"))
    })
    private Location to;

    @ElementCollection
    @AttributeOverrides({
            @AttributeOverride(name="title", column=@Column(name="stop_title")),
            @AttributeOverride(name="latitude", column=@Column(name="stop_lat")),
            @AttributeOverride(name="longitude", column=@Column(name="stop_lng")),
            @AttributeOverride(name="address", column=@Column(name="stop_address"))
    })
    private List<Location> stops = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RegisteredUser getUser() {
        return user;
    }

    public void setUser(RegisteredUser user) {
        this.user = user;
    }

    public Location getFrom() {
        return from;
    }

    public void setFrom(Location from) {
        this.from = from;
    }

    public Location getTo() {
        return to;
    }

    public void setTo(Location to) {
        this.to = to;
    }

    public List<Location> getStops() {
        return stops;
    }

    public void setStops(List<Location> stops) {
        this.stops = stops;
    }
}


