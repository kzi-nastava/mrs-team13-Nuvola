package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.dto.CreatedReviewDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Integer vehicleRating;

    @Column(nullable = true)
    private Integer driverRating;

    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id")
    private Ride ride;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private RegisteredUser author;

    public Rating(Integer vehicleRating, Integer driverRating, String comment) {}

}
