package org.example.ecommercejavafx.customer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.User;
import org.example.ecommercejavafx.DbConnection;

public class EditProfileView {
    private final int userId;
    private final User user;

    public EditProfileView(int userId, User user) {
        this.userId = userId;
        this.user = user;
    }

    public void start(Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("Edit Profile");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField usernameField = new TextField(user.getUsername());
        usernameField.setPromptText("Enter new username");
        usernameField.setStyle("-fx-padding: 10px;");

        TextField emailField = new TextField(user.getEmail());
        emailField.setPromptText("Enter new email");
        emailField.setStyle("-fx-padding: 10px;");

        TextField addressField = new TextField(user.getAddress());
        addressField.setPromptText("Enter new address");
        addressField.setStyle("-fx-padding: 10px;");

        ComboBox<String> paymentMethodComboBox = new ComboBox<>();
        paymentMethodComboBox.getItems().addAll("Credit Card", "PayPal", "Cash");
        paymentMethodComboBox.setValue(user.getPaymentMethod());
        paymentMethodComboBox.setStyle("-fx-padding: 10px;");

        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.setStyle("-fx-padding: 10px;");
        changePasswordButton.setOnAction(e -> openPasswordChangeDialog(stage));

        Button saveButton = new Button("Save Changes");
        saveButton.setStyle("-fx-padding: 10px;");
        saveButton.setOnAction(e -> {
            user.setUsername(usernameField.getText());
            user.setEmail(emailField.getText());
            user.setAddress(addressField.getText());
            user.setPaymentMethod(paymentMethodComboBox.getValue());

            DbConnection.updateUserProfile(userId, user);

            System.out.println("Profile updated for user: " + userId);

            stage.close();
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(saveButton, changePasswordButton);

        root.getChildren().addAll(
                titleLabel,
                usernameField, emailField, addressField,
                new Label("Payment Method:"), paymentMethodComboBox,
                buttonBox
        );

        Scene scene = new Scene(root, 400, 350);
        stage.setScene(scene);
        stage.setTitle("Edit Profile");
        stage.show();
    }

    private void openPasswordChangeDialog(Stage stage) {
        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Change Password");
        passwordDialog.setHeaderText("Enter your new password:");

        passwordDialog.showAndWait().ifPresent(newPassword -> {
            if (!newPassword.isEmpty()) {
                DbConnection.updateUserPassword(userId, newPassword);
                System.out.println("Password updated for user: " + userId);
            }
        });
    }
}
