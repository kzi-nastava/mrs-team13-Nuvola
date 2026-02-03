package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.CreateReviewDTO;
import Nuvola.Projekatsiit2025.dto.CreatedReviewDTO;
import Nuvola.Projekatsiit2025.exceptions.RideNotFoundException;
import Nuvola.Projekatsiit2025.model.Rating;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.repositories.RatingRepository;
import Nuvola.Projekatsiit2025.repositories.RegisteredUserRepository;
import Nuvola.Projekatsiit2025.repositories.RideRepository;
import Nuvola.Projekatsiit2025.services.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RatingServiceImpl implements RatingService {

    @Autowired
    RatingRepository ratingRepository;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    RegisteredUserRepository registeredUserRepository;

    @Override
    public void addRating(CreateReviewDTO createReviewDTO) {
        Rating rating = new Rating();
        rating.setDriverRating(createReviewDTO.getDriverRating());
        rating.setVehicleRating(createReviewDTO.getVehicleRating());
        rating.setComment(createReviewDTO.getComment());
        //test
//        Ride ride = rideRepository.findById(createReviewDTO.getRideId()).orElse(null);
//        if (ride == null) throw new RideNotFoundException("Ride not found");
//        RegisteredUser author = registeredUserRepository.findByUsername(createReviewDTO.getUsername());
//        if (author == null) throw new RuntimeException("User not found");
//        rating.setRide(ride);
//        rating.setAuthor(author);
        ratingRepository.save(rating);
    }
}
