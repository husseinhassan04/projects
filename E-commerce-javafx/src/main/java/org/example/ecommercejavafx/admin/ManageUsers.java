package org.example.ecommercejavafx.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User;
import org.example.ecommercejavafx.DbConnection;
import org.example.ecommercejavafx.customer.EditProfileView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManageUsers {

    public VBox showManageUsersWindow() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label title = new Label("Manage Users");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<User> userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> data.getValue().idProperty().asObject());

        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(data -> data.getValue().usernameProperty());

        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());

        TableColumn<User, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(data -> data.getValue().roleProperty());

        TableColumn<User, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setCellValueFactory(data -> data.getValue().addressProperty());

        TableColumn<User, String> paymentMethodColumn = new TableColumn<>("Payment Method");
        paymentMethodColumn.setCellValueFactory(data -> data.getValue().paymentMethodProperty());

        TableColumn<User, User> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellValueFactory(param -> new javafx.beans.property.ReadOnlyObjectWrapper<>(param.getValue()));
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");
            private final Button editButton = new Button("Edit");

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (user == null) {
                    setGraphic(null);
                    return;
                }

                HBox buttonBox = new HBox(10, editButton, removeButton);
                buttonBox.setAlignment(Pos.CENTER);
                setGraphic(buttonBox);

                editButton.setOnAction(event -> {
                    openEditUserDialog(user);
                });

                removeButton.setOnAction(event -> {
                    removeUser(user);
                    userTable.getItems().remove(user);
                });
            }
        });

        userTable.getColumns().addAll(idColumn, usernameColumn, emailColumn, roleColumn, addressColumn, paymentMethodColumn, actionColumn);
        userTable.getItems().addAll(DbConnection.fetchUsers());

        Button addUserButton = new Button("Add User");
        addUserButton.setOnAction(event -> openAddUserDialog(userTable));

        layout.getChildren().addAll(title, userTable, addUserButton);
        return layout;
    }

    private void openEditUserDialog(User user) {
        EditProfileView editProfileView = new EditProfileView(user.getId(), user);
        Stage stage = new Stage();
        editProfileView.start(stage);
    }

    private void openAddUserDialog(TableView<User> userTable) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New User");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Admin", "Customer");
        roleComboBox.setPromptText("Select Role");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");

        TextField addressField = new TextField();
        addressField.setPromptText("Enter Address");

        ComboBox<String> paymentMethodComboBox = new ComboBox<>();
        paymentMethodComboBox.getItems().addAll("Credit Card", "PayPal", "Cash");
        paymentMethodComboBox.setPromptText("Select Payment Method");

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String role = roleComboBox.getValue();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            String paymentMethod = paymentMethodComboBox.getValue();

            if (username.isEmpty() || password.isEmpty() || role == null || email.isEmpty() || address.isEmpty() || paymentMethod == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required!");
                return;
            }

            DbConnection.addUserToDatabase(username, password, role, email, address, paymentMethod);
            userTable.getItems().setAll(DbConnection.fetchUsers());
            dialog.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> dialog.close());

        HBox buttonBox = new HBox(10, addButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(
                new Label("Add New User"),
                usernameField,
                passwordField,
                roleComboBox,
                emailField,
                addressField,
                paymentMethodComboBox,
                buttonBox
        );

        Scene scene = new Scene(layout, 300, 350);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void removeUser(User user) {
        DbConnection.removeUser(user);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
