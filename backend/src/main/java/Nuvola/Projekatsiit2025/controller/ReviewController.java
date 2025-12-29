package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.CreateReviewDTO;
import Nuvola.Projekatsiit2025.dto.CreatedDriverDTO;
import Nuvola.Projekatsiit2025.dto.CreatedReviewDTO;
import Nuvola.Projekatsiit2025.dto.CreatedRideDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    //2.8
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CreatedReviewDTO> createReview(@RequestBody CreateReviewDTO createReviewDTO) {
        CreatedReviewDTO review = new CreatedReviewDTO();
        return new ResponseEntity<CreatedReviewDTO>(review,HttpStatus.OK);
    }
}
