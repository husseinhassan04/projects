package models;

import java.time.LocalDateTime;

public class Review {
    private final int reviewId;
    private final int productId;
    private final String username;
    private final int rating;
    private final String reviewText;
    private final LocalDateTime reviewDate;

    public Review(int reviewId, int productId, String username, int rating, String reviewText, LocalDateTime reviewDate) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.username = username;
        this.rating = rating;
        this.reviewText = reviewText;
        this.reviewDate = reviewDate;
    }


    public String getUsername() {
        return username;
    }

    public int getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getReviewDate() {
        return reviewDate.toString();
    }
}
