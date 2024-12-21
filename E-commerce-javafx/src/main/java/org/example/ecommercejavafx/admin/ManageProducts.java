package org.example.ecommercejavafx.admin;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Product;
import org.example.ecommercejavafx.DbConnection;

import java.util.List;

public class ManageProducts {

    public VBox showManageProductsWindow() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label title = new Label("Manage Products");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Product> productTable = new TableView<>();
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));

        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getName()));

        TableColumn<Product, String> descColumn = new TableColumn<>("Description");
        descColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDescription()));

        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPrice()));

        TableColumn<Product, Integer> stockColumn = new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStock()));

        TableColumn<Product, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    DbConnection.removeProduct(product.getId());
                    getTableView().getItems().remove(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });

        TableColumn<Product, Void> reviewsActionColumn = new TableColumn<>("Reviews");
        reviewsActionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewReviewsButton = new Button("View Reviews");

            {
                viewReviewsButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showReviewsWindow(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewReviewsButton);
            }
        });

        productTable.getColumns().addAll(idColumn, nameColumn, descColumn, priceColumn, stockColumn, actionColumn, reviewsActionColumn);
        productTable.getItems().addAll(DbConnection.fetchProducts());

        HBox addProductForm = new HBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        TextField descField = new TextField();
        descField.setPromptText("Description");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        TextField stockField = new TextField();
        stockField.setPromptText("Stock");
        TextField imageUrlField = new TextField();
        imageUrlField.setPromptText("Image URL");

        Button addButton = new Button("Add Product");

        addButton.setOnAction(event -> {
            String name = nameField.getText();
            String desc = descField.getText();
            double price = Double.parseDouble(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());
            String imageUrl = imageUrlField.getText();

            DbConnection.addProductToDatabase(name, desc, price, stock, imageUrl);
            productTable.getItems().setAll(DbConnection.fetchProducts());

            nameField.clear();
            descField.clear();
            priceField.clear();
            stockField.clear();
            imageUrlField.clear();
        });

        addProductForm.getChildren().addAll(nameField, descField, priceField, stockField, imageUrlField, addButton);

        layout.getChildren().addAll(title, addProductForm, productTable);

        return layout;
    }

    private void showReviewsWindow(Product product) {
        Stage reviewsStage = new Stage();
        reviewsStage.setTitle("Product Reviews for " + product.getName());

        VBox reviewsLayout = new VBox(15);
        reviewsLayout.setPadding(new Insets(20));
        reviewsLayout.setStyle("-fx-background-color: #f4f4f4; -fx-border-radius: 10px;");

        Label title = new Label("Reviews for " + product.getName());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        ScrollPane reviewsScrollPane = new ScrollPane();
        reviewsScrollPane.setFitToWidth(true);
        reviewsScrollPane.setStyle("-fx-background-color: transparent;");

        VBox reviewsContainer = new VBox(10);
        reviewsContainer.setPadding(new Insets(10));

        List<String> reviews = DbConnection.fetchReviewsForProduct(product.getId());

        for (String review : reviews) {
            String[] parts = review.split("\n", 2);
            String ratingPart = parts[0];
            String reviewText = parts.length > 1 ? parts[1] : "";

            String username = ratingPart.split(" - ")[0];
            String ratingStr = ratingPart.split(":")[1].trim();
            int rating = ratingStr.length();

            HBox reviewBox = new HBox(10);
            reviewBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-radius: 5px; -fx-padding: 10px;");
            reviewBox.setPrefWidth(350);

            VBox reviewContent = new VBox(5);
            reviewContent.setStyle("-fx-pref-width: 300px;");

            Label usernameLabel = new Label(username);
            usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0078d4;");

            HBox ratingBox = new HBox(5);
            for (int i = 0; i < 5; i++) {
                Label star = new Label(i < rating ? "★" : "☆");
                star.setStyle("-fx-font-size: 18px; -fx-text-fill: gold;");
                ratingBox.getChildren().add(star);
            }

            Label reviewDetails = new Label(reviewText);
            reviewDetails.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

            reviewContent.getChildren().addAll(usernameLabel, ratingBox, reviewDetails);
            reviewBox.getChildren().add(reviewContent);
            reviewsContainer.getChildren().add(reviewBox);
        }

        reviewsScrollPane.setContent(reviewsContainer);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-font-size: 14px;");
        closeButton.setOnAction(e -> reviewsStage.close());

        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        reviewsLayout.getChildren().addAll(title, reviewsScrollPane, buttonBox);

        Scene scene = new Scene(reviewsLayout, 450, 400);
        reviewsStage.setScene(scene);
        reviewsStage.show();
    }
}
