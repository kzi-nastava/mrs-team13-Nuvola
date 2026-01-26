package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
public class Driver extends User {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivitySession> sessions =  new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_DRIVER"));
    }

    public Driver() {
        super();
    }

    public Driver(Long id, String email, String password, String firstName,
                  String lastName, String address, String phone, String picture,
                  DriverStatus status, List<ActivitySession> sessions, Vehicle vehicle) {

        super(id, email, password, firstName, lastName, address, phone, picture);
        this.status = status;
        this.sessions = sessions;
        this.vehicle = vehicle;
    }

    // TODO: function/s that's going to be called while logging in or out to update the sessions
    //  (delete old sessions and add/update new ones)

    // !!! this getter should be modified to support dealing with sessions
    // before returning value it should check if the 8h per 24h limit is exceeded
    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }

    public List<ActivitySession> getSessions() {
        return sessions;
    }

    public void setSessions(List<ActivitySession> sessions) {
        this.sessions = sessions;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}
