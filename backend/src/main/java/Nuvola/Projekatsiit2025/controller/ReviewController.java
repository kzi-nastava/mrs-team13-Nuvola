package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.CreateReviewDTO;
import Nuvola.Projekatsiit2025.dto.CreatedDriverDTO;
import Nuvola.Projekatsiit2025.exceptions.RideNotFoundException;
import Nuvola.Projekatsiit2025.services.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    RatingService ratingService;

    //2.8
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createReview(@RequestBody CreateReviewDTO createReviewDTO) {
        try {
            ratingService.addRating(createReviewDTO);
        } catch (RideNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }
}
