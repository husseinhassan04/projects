package org.example.ecommercejavafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import models.User;

public class LoginPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 40; -fx-alignment: center;");
        root.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));

        Label titleLabel = new Label("Welcome to E-Commerce");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-padding: 10px; -fx-border-radius: 5px; -fx-background-color: white;");

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle("-fx-padding: 10px; -fx-border-radius: 5px; -fx-background-color: white;");

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-padding: 10px 20px; -fx-background-color: #00b4d8; -fx-text-fill: white; -fx-border-radius: 5px;");

        Button signUpButton = new Button("Don't have an Account?\n Sign up here");
        signUpButton.setStyle("-fx-padding: 10px 20px; -fx-background-color: #6c757d; -fx-text-fill: white; -fx-border-radius: 5px;");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px;");

        root.getChildren().addAll(
                titleLabel,
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                loginButton,
                signUpButton,
                statusLabel
        );

        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            User user = DbConnection.authenticateUser(username, password);
            if (user != null) {
                statusLabel.setText("Login Successful!");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                openDashboard(primaryStage, user);
            } else {
                statusLabel.setText("Invalid Credentials");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });

        signUpButton.setOnAction(event -> {
            SignUpPage signUpPage = new SignUpPage();
            signUpPage.start(primaryStage);
        });

        Scene scene = new Scene(root, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login Page");
        primaryStage.show();
    }

    private void openDashboard(Stage stage, User user) {
        if ("admin".equals(user.getRole())) {
            AdminDashboard adminDashboard = new AdminDashboard(user.getUsername());
            adminDashboard.start(stage);
        } else if ("customer".equals(user.getRole())) {
            CustomerDashboard customerDashboard = new CustomerDashboard(user);
            customerDashboard.start(stage);
        } else {
            showAlert("Unknown Role", "Access Denied", "You do not have permission to access this system.");
        }
    }

    private void showAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
