package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.services.UserService;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    @Column(unique = true, nullable = false)
    protected String username;
    @Column(unique = true, nullable = false)
    protected String email;
    @Column(nullable = false)
    protected String password;
    @Column(nullable = false)
    protected String firstName;
    @Column(nullable = false)
    protected String lastName;
    @Column(nullable = false)
    protected String address;
    @Column(nullable = false)
    protected String phone;
    private Timestamp lastPasswordResetDate;

    protected String picture;

    @Column(nullable = false)
    protected boolean isBlocked;

    protected String blockingReason;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    protected List<Notification> notifications = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    public User() {
    }

    public User(Long id, String email, String password, String firstName,
                String lastName, String address, String phone, String picture) {
        super();
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
        this.picture = picture;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
