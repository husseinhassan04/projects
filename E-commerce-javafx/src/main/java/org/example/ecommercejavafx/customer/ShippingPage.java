package org.example.ecommercejavafx.customer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Promotion;
import models.ShippingProvider;
import models.User;
import org.example.ecommercejavafx.DbConnection;

import java.util.List;

public class ShippingPage {
    private final int userId;
    private final double totalPrice;
    private final CartService cartService;
    private double finalPrice;

    public ShippingPage(int userId, double totalPrice, CartService cartService) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.cartService = cartService;
        this.finalPrice = totalPrice;
    }

    public void show() {
        Stage shippingStage = new Stage();
        shippingStage.setTitle("Shipping Information");

        User user = DbConnection.getUserInfo(userId);

        List<ShippingProvider> shippingProviders = DbConnection.getShippingProviders();

        if (user == null) {
            showAlert("Error fetching user information.");
            return;
        }

        if (shippingProviders.isEmpty()) {
            showAlert("No shipping providers available.");
            return;
        }

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(15);
        gridPane.setHgap(15);
        gridPane.setAlignment(Pos.CENTER);

        Label nameLabel = new Label("Username:");
        TextField nameField = new TextField(user.getUsername());
        nameField.setEditable(false);
        gridPane.add(nameLabel, 0, 0);
        gridPane.add(nameField, 1, 0);

        Label addressLabel = new Label("Address:");
        TextField addressField = new TextField(user.getAddress());
        addressField.setEditable(false);
        gridPane.add(addressLabel, 0, 1);
        gridPane.add(addressField, 1, 1);

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField(user.getEmail());
        emailField.setEditable(false);
        gridPane.add(emailLabel, 0, 2);
        gridPane.add(emailField, 1, 2);

        Label paymentLabel = new Label("Payment Method:");
        TextField paymentField = new TextField(user.getPaymentMethod());
        paymentField.setEditable(false);
        gridPane.add(paymentLabel, 0, 3);
        gridPane.add(paymentField, 1, 3);

        Label providerLabel = new Label("Shipment Provider:");
        ComboBox<ShippingProvider> providerComboBox = new ComboBox<>();
        providerComboBox.getItems().addAll(shippingProviders);
        providerComboBox.setPromptText("Select Provider");
        gridPane.add(providerLabel, 0, 4);
        gridPane.add(providerComboBox, 1, 4);

        Label promoLabel = new Label("Promotion Code:");
        TextField promoField = new TextField();
        gridPane.add(promoLabel, 0, 5);
        gridPane.add(promoField, 1, 5);

        Button applyPromoButton = new Button("Apply");
        gridPane.add(applyPromoButton, 2, 5);

        Label totalLabel = new Label("Total Price:");
        Label totalPriceLabel = new Label(String.format("$%.2f", totalPrice));
        gridPane.add(totalLabel, 0, 6);
        gridPane.add(totalPriceLabel, 1, 6);

        Label finalPriceLabel = new Label("Final Price:");
        Label finalPriceTextLabel = new Label(String.format("$%.2f", finalPrice));
        gridPane.add(finalPriceLabel, 0, 7);
        gridPane.add(finalPriceTextLabel, 1, 7);

        applyPromoButton.setOnAction(event -> {
            String promoCode = promoField.getText().trim();
            if (!promoCode.isEmpty()) {
                Promotion promotion = DbConnection.validatePromoCode(promoCode);
                if (promotion != null) {
                    double discount = (promotion.getDiscountPercentage() / 100) * totalPrice;
                    finalPrice = totalPrice - discount;
                    finalPriceTextLabel.setText(String.format("$%.2f", finalPrice));
                    showAlert("Promotion applied! Discount: " + promotion.getDiscountPercentage() + "%");
                } else {
                    showAlert("Invalid promotion code.");
                }
            } else {
                showAlert("Please enter a promotion code.");
            }
        });

        providerComboBox.setOnAction(event -> {
            ShippingProvider selectedProvider = providerComboBox.getValue();
            if (selectedProvider != null) {
                finalPrice = totalPrice + selectedProvider.getPrice();
                finalPriceTextLabel.setText(String.format("$%.2f", finalPrice));
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> shippingStage.close());

        Button confirmButton = new Button("Confirm and Pay");
        confirmButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px;");
        confirmButton.setOnAction(event -> {
            ShippingProvider selectedProvider = providerComboBox.getValue();
            if (selectedProvider == null) {
                showAlert("Please select a shipment provider.");
            } else {
                cartService.completeCheckout(selectedProvider, finalPrice);
                shippingStage.close();
            }
        });

        VBox layout = new VBox(20, gridPane, backButton, confirmButton);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 600);
        shippingStage.setScene(scene);
        shippingStage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
