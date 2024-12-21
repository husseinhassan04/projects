package org.example.ecommercejavafx.customer;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Product;
import org.example.ecommercejavafx.DbConnection;

public class ProductService {
    private final WishlistView wishlistView;
    private final int userId;

    public ProductService(WishlistView wishlistView, int userId) {
        this.wishlistView = wishlistView;
        this.userId = userId;
    }

    public VBox openBrowseProducts() {
        VBox layout = new VBox(20);
        layout.setStyle("-fx-padding: 20;");
        layout.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Browse Products");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.setStyle("-fx-font-size: 16px; -fx-padding: 10;");

        ScrollPane scrollPane = new ScrollPane();
        VBox productListView = new VBox(20);
        loadProducts(productListView, "");

        scrollPane.setContent(productListView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            productListView.getChildren().clear();
            loadProducts(productListView, newValue);
        });

        layout.getChildren().addAll(title, searchField, scrollPane);

        return layout;
    }

    private void loadProducts(VBox productListView, String query) {
        try {
            var products = DbConnection.getProducts(query);
            for (Product product : products) {
                VBox productBox = createProductBox(product);
                productListView.getChildren().add(productBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createProductBox(Product product) {
        VBox productBox = new VBox(15);
        productBox.setStyle("-fx-padding: 15; -fx-border-radius: 8; -fx-background-color: #f9f9f9; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 3);");

        Image image = new Image(product.getImageUrl());
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label("Name: " + product.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label descLabel = new Label( product.getDescription());
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        Label priceLabel = new Label("Price: $" + String.format("%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");
        Label stockLabel = new Label("Stock: " + product.getStock());
        stockLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");

        Label quantityLabel = new Label("Quantity:");
        quantityLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");

        Spinner<Integer> quantitySpinner = new Spinner<>(1, product.getStock(), 1);
        quantitySpinner.setStyle("-fx-font-size: 16px; -fx-padding: 5;");

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-size: 14px; -fx-border-radius: 8; -fx-pref-width: 120;");
        addToCartButton.setOnAction(event -> {
            int quantity = quantitySpinner.getValue();
            if (userId < 0) {
                showAlert("Login Required", "You are currently in guest mode. Please login first.");
            } else if (quantity <= 0) {
                showAlert("Invalid Quantity", "Please select a valid quantity.");
            } else {
                new CartService(userId).addToCart(product, quantity);
                showAlert("Cart", product.getName() + " (" + quantity + ") added to cart!");
            }
        });

        Button addToWishlistButton = new Button("Add to Wishlist");
        addToWishlistButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-size: 14px; -fx-border-radius: 8; -fx-pref-width: 120;");
        addToWishlistButton.setOnAction(event -> {
            if (userId < 0) {
                showAlert("Login Required", "You are currently in guest mode. Please login first.");
            } else {
                wishlistView.addToWishlist(product);
            }
        });

        Button viewDetailsButton = new Button("View Details");
        viewDetailsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-size: 14px; -fx-border-radius: 8; -fx-pref-width: 120;");
        viewDetailsButton.setOnAction(event -> new ProductDetailsView(product, userId).show());

        HBox buttonBox = new HBox(10, addToWishlistButton, addToCartButton, viewDetailsButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox detailsBox = new VBox(5, nameLabel,descLabel, priceLabel, stockLabel, quantityLabel, quantitySpinner);
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        VBox productContent = new VBox(15);
        productContent.setAlignment(Pos.CENTER);
        productContent.getChildren().addAll(imageView, detailsBox, buttonBox);

        productBox.getChildren().add(productContent);
        return productBox;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
