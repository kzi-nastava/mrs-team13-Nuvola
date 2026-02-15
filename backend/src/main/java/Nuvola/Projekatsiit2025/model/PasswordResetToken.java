//package Nuvola.Projekatsiit2025.model;
//
//import jakarta.persistence.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name="password_reset_tokens") // opcionalno
//public class PasswordResetToken {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(unique = true, nullable = false)
//    private String token;
//
//    @OneToOne
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    private LocalDateTime expiresAt;
//
//    private boolean used = false;
//
//    public Long getId() { return id; }
//
//    public String getToken() { return token; }
//    public void setToken(String token) { this.token = token; }
//
//    public User getUser() { return user; }
//    public void setUser(User user) { this.user = user; }
//
//    public LocalDateTime getExpiresAt() { return expiresAt; }
//    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
//
//    public boolean isUsed() { return used; }
//    public void setUsed(boolean used) { this.used = used; }
//}


package Nuvola.Projekatsiit2025.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;
}
