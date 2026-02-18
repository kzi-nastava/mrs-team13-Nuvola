package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.CreateReviewDTO;
import Nuvola.Projekatsiit2025.exceptions.RatingAlreadyExistsException;
import Nuvola.Projekatsiit2025.exceptions.RatingTimeExpiredException;
import Nuvola.Projekatsiit2025.exceptions.UserNotFoundException;
import Nuvola.Projekatsiit2025.exceptions.ride.RideNotFoundException;
import Nuvola.Projekatsiit2025.model.Rating;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.repositories.RatingRepository;
import Nuvola.Projekatsiit2025.repositories.RegisteredUserRepository;
import Nuvola.Projekatsiit2025.repositories.RideRepository;
import Nuvola.Projekatsiit2025.services.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.UnknownServiceException;

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



        Ride ride = rideRepository.findById(createReviewDTO.getRideId()).orElse(null);
        if (ride == null) throw new RideNotFoundException("Ride not found");
        RegisteredUser author = registeredUserRepository.findByUsername(createReviewDTO.getUsername());
        if (author == null) throw new UserNotFoundException("Registered user " + createReviewDTO.getUsername() + " not found");

        // chack if this user already rated this ride
        if (ratingRepository.existsByRideIdAndAuthorId(ride.getId(), author.getId())) {
            throw new RatingAlreadyExistsException("User " + createReviewDTO.getUsername() + " has already rated this ride");
        }

        // check if the time for rating has expired (more than 3 days after the ride ended)
        if (ride.getEndTime() == null || ride.getEndTime().plusDays(3).isBefore(java.time.LocalDateTime.now())) {
            throw new RatingTimeExpiredException();
        }

        Rating rating = new Rating();
        rating.setRide(ride);
        rating.setAuthor(author);
        rating.setDriverRating(createReviewDTO.getDriverRating());
        rating.setVehicleRating(createReviewDTO.getVehicleRating());
        rating.setComment(createReviewDTO.getComment());
        ratingRepository.saveAndFlush(rating);
    }
}
