package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.CreateReviewDTO;
import Nuvola.Projekatsiit2025.dto.CreatedReviewDTO;

public interface RatingService {

    public void addRating(CreateReviewDTO createdReviewDTO);

}
