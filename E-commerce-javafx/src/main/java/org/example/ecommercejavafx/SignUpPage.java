package org.example.ecommercejavafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SignUpPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 40; -fx-alignment: center; -fx-spacing: 10;");
        root.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));

        Label titleLabel = new Label("Create an Account");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-padding: 10px; -fx-border-radius: 5px; -fx-background-color: white;");

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle("-fx-padding: 10px; -fx-border-radius: 5px; -fx-background-color: white;");

        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter your password");
        confirmPasswordField.setStyle("-fx-padding: 10px; -fx-border-radius: 5px; -fx-background-color: white;");

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle("-fx-padding: 10px; -fx-border-radius: 5px; -fx-background-color: white;");

        Label addressLabel = new Label("Address:");
        TextField addressField = new TextField();
        addressField.setPromptText("Enter your address");
        addressField.setStyle("-fx-padding: 10px; -fx-border-radius: 5px; -fx-background-color: white;");

        Label paymentMethodLabel = new Label("Payment Method:");
        ComboBox<String> paymentMethodComboBox = new ComboBox<>();
        paymentMethodComboBox.getItems().addAll("Credit Card", "PayPal", "Cash");
        paymentMethodComboBox.setPromptText("Select payment method");
        paymentMethodComboBox.setStyle("-fx-padding: 10px; -fx-border-radius: 5px;");

        Button signUpButton = new Button("Sign Up");
        signUpButton.setStyle("-fx-padding: 10px 20px; -fx-background-color: #00b4d8; -fx-text-fill: white; -fx-border-radius: 5px;");

        Button backToLoginButton = new Button("Login");
        backToLoginButton.setStyle("-fx-padding: 10px 20px; -fx-background-color: #6c757d; -fx-text-fill: white; -fx-border-radius: 5px;");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px;");

        root.getChildren().addAll(
                titleLabel,
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                confirmPasswordLabel, confirmPasswordField,
                emailLabel, emailField,
                addressLabel, addressField,
                paymentMethodLabel, paymentMethodComboBox,
                signUpButton,
                backToLoginButton,
                statusLabel
        );

        signUpButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String email = emailField.getText();
            String address = addressField.getText();
            String paymentMethod = paymentMethodComboBox.getValue();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty() || address.isEmpty() || paymentMethod == null) {
                statusLabel.setText("All fields are required!");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else if (!password.equals(confirmPassword)) {
                statusLabel.setText("Passwords do not match!");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else {
                boolean success = DbConnection.registerUser(username, password, email, address, paymentMethod);
                if (success) {
                    statusLabel.setText("Sign Up Successful! Please log in.");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                } else {
                    statusLabel.setText("Registration failed. Try again.");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });

        backToLoginButton.setOnAction(event -> {
            LoginPage loginPage = new LoginPage();
            loginPage.start(primaryStage);
        });

        Scene scene = new Scene(root, 500, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sign Up Page");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
