package org.example.ecommercejavafx.customer;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.Product;
import org.example.ecommercejavafx.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WishlistView extends VBox {
    private final int userId;
    private final ListView<Product> wishlistListView;

    public WishlistView(int userId) {
        this.userId = userId;

        wishlistListView = new ListView<>();
        wishlistListView.prefWidthProperty().bind(this.widthProperty());

        refreshWishlist();

        wishlistListView.setCellFactory(param -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (product != null) {
                    HBox hBox = new HBox(15);
                    hBox.setPrefHeight(100);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    hBox.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 1; -fx-background-color: #f9f9f9;");

                    ImageView imageView = new ImageView(new Image(product.getImageUrl()));
                    imageView.setFitHeight(80);
                    imageView.setFitWidth(80);
                    imageView.setStyle("-fx-effect: dropshadow(gaussian, gray, 10, 0.5, 1, 1);");

                    VBox vBox = new VBox(5);
                    Text name = new Text(product.getName());
                    name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                    Text price = new Text("Price: $" + product.getPrice());
                    price.setStyle("-fx-font-size: 12px;");
                    Text stock = new Text("Stock: " + product.getStock());
                    stock.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
                    vBox.getChildren().addAll(name, price, stock);

                    Button removeButton = new Button("❌");
                    removeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 16px;");
                    removeButton.setOnAction(event -> removeFromWishlist(product));

                    removeButton.setOnMouseEntered(event -> removeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: darkred; -fx-font-size: 16px;"));
                    removeButton.setOnMouseExited(event -> removeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 16px;"));

                    hBox.getChildren().addAll(imageView, vBox, removeButton);
                    setGraphic(hBox);
                } else {
                    setGraphic(null);
                }
            }
        });

        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(wishlistListView);
    }

    public void refreshWishlist() {
        wishlistListView.getItems().setAll(DbConnection.getWishlist(userId));
    }

    public void addToWishlist(Product product) {
        if (DbConnection.isProductInWishlist(userId, product)) {
            showAlert("Wishlist", product.getName() + " is already in your wishlist!");
            return;
        }

        DbConnection.addToWishlist(userId, product);
        refreshWishlist();
        showAlert("Wishlist", product.getName() + " added to wishlist!");
    }

    private void removeFromWishlist(Product product) {
        DbConnection.removeFromWishlist(userId, product);
        refreshWishlist();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
