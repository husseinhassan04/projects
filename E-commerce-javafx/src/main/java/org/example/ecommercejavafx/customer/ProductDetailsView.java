package org.example.ecommercejavafx.customer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Product;
import models.Review;
import org.example.ecommercejavafx.DbConnection;

import java.util.List;

public class ProductDetailsView {

    private final Product product;
    private final int currentUserId;

    public ProductDetailsView(Product product, int currentUserId) {
        this.product = product;
        this.currentUserId = currentUserId;
    }

    public void show() {
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Product Details");

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setStyle("-fx-background-color: #f9f9f9;");

        VBox productDetailsLayout = new VBox(15);
        productDetailsLayout.setPadding(new Insets(20));
        productDetailsLayout.setStyle("-fx-background-color: white; -fx-border-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 5);");
        productDetailsLayout.setMaxWidth(600);

        Label nameLabel = new Label("Product: " + product.getName());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label priceLabel = new Label("Price: $" + String.format("%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #28a745;");

        Label stockLabel = new Label("Stock: " + product.getStock());
        stockLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");

        Label averageRatingLabel = new Label("Average Rating: " + product.getRating());
        averageRatingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ffc107;");

        productDetailsLayout.getChildren().addAll(nameLabel, priceLabel, stockLabel, averageRatingLabel);

        TitledPane reviewsPane = new TitledPane();
        reviewsPane.setText("Customer Reviews");
        reviewsPane.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        reviewsPane.setExpanded(true);

        VBox reviewsLayout = new VBox(10);
        reviewsLayout.setPadding(new Insets(10));
        reviewsLayout.setStyle("-fx-background-color: white; -fx-border-radius: 10px;");

        ListView<HBox> reviewsListView = new ListView<>();
        reviewsListView.setPrefHeight(300);
        loadReviews(reviewsListView, averageRatingLabel);
        reviewsLayout.getChildren().add(reviewsListView);

        reviewsPane.setContent(reviewsLayout);

        TitledPane addReviewPane = new TitledPane();
        addReviewPane.setText("Write Your Review");
        addReviewPane.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        addReviewPane.setExpanded(true);

        VBox addReviewLayout = new VBox(15);
        addReviewLayout.setPadding(new Insets(15));
        addReviewLayout.setStyle("-fx-background-color: white; -fx-border-radius: 10px;");

        Label ratingLabel = new Label("Rating (1-5):");
        ratingLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        HBox ratingLayout = new HBox(5);
        ratingLayout.setAlignment(Pos.CENTER);
        ToggleGroup ratingGroup = new ToggleGroup();
        for (int i = 1; i <= 5; i++) {
            RadioButton star = new RadioButton("★");
            star.setStyle("-fx-font-size: 30px; -fx-text-fill: #ccc;");
            star.setToggleGroup(ratingGroup);
            star.setUserData(i);
            ratingLayout.getChildren().add(star);
        }

        TextArea reviewInput = new TextArea();
        reviewInput.setPromptText("Write your review...");
        reviewInput.setWrapText(true);
        reviewInput.setPrefHeight(120);
        reviewInput.setStyle("-fx-font-size: 14px;");

        Button submitReviewButton = new Button("Submit Review");
        submitReviewButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-border-radius: 8px; padding: 10px 20px;");
        submitReviewButton.setOnAction(e -> {
            if (currentUserId < 0) {
                showAlert("Login Required", "You are currently in guest mode\n please login first");
            } else {
                String reviewText = reviewInput.getText().trim();
                Toggle selectedToggle = ratingGroup.getSelectedToggle();
                if (selectedToggle != null && !reviewText.isEmpty()) {
                    int rating = (int) selectedToggle.getUserData();
                    if (DbConnection.addReview(product.getId(), currentUserId, rating, reviewText)) {
                        reviewsListView.getItems().add(createReviewCard("You", rating, "Just Now", reviewText));
                        reviewInput.clear();
                        ratingGroup.getSelectedToggle().setSelected(false);
                        updateAverageRating(averageRatingLabel);
                        showAlert("Review Submitted", "Thank you for your feedback!");
                    } else {
                        showAlert("Error", "Failed to submit your review.");
                    }
                } else {
                    showAlert("Error", "Rating and review text are required.");
                }
            }
        });

        addReviewLayout.getChildren().addAll(ratingLabel, ratingLayout, reviewInput, submitReviewButton);
        addReviewPane.setContent(addReviewLayout);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> detailsStage.close());
        closeButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-border-radius: 8px; padding: 10px 20px;");

        mainLayout.getChildren().addAll(productDetailsLayout, reviewsPane, addReviewPane, closeButton);

        Scene scene = new Scene(mainLayout, 650, 800);
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    private void loadReviews(ListView<HBox> reviewsListView, Label averageRatingLabel) {
        List<Review> reviews = DbConnection.getReviews(product.getId());
        for (Review review : reviews) {
            reviewsListView.getItems().add(createReviewCard(
                    review.getUsername(),
                    review.getRating(),
                    review.getReviewDate(),
                    review.getReviewText()
            ));
        }
        updateAverageRating(averageRatingLabel);
    }

    private void updateAverageRating(Label averageRatingLabel) {
        double newRating = DbConnection.calculateAverageRating(product.getId());
        product.setRating(newRating);
        averageRatingLabel.setText("Average Rating: " + String.format("%.2f", newRating));
        DbConnection.updateProductRating(product.getId(), newRating);
    }

    private HBox createReviewCard(String username, int rating, String date, String reviewText) {
        HBox reviewCard = new HBox(15);
        reviewCard.setPadding(new Insets(10));
        reviewCard.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 5);");

        VBox textLayout = new VBox(5);
        textLayout.setAlignment(Pos.TOP_LEFT);

        Label userLabel = new Label(username + " - " + date);
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label ratingLabel = new Label("Rating: " + "★".repeat(rating));
        ratingLabel.setStyle("-fx-text-fill: #ffc107;");

        Label reviewLabel = new Label(reviewText);
        reviewLabel.setWrapText(true);
        reviewLabel.setStyle("-fx-font-size: 14px;");

        textLayout.getChildren().addAll(userLabel, ratingLabel, reviewLabel);

        reviewCard.getChildren().add(textLayout);

        return reviewCard;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
