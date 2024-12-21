package org.example.ecommercejavafx.admin;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Promotion;
import org.example.ecommercejavafx.DbConnection;

import java.time.LocalDate;

public class ManagePromotions {

    public VBox showManagePromotionsWindow() {
        VBox promotionsView = new VBox(10);
        promotionsView.setStyle("-fx-padding: 20;");

        Label titleLabel = new Label("Manage Promotions");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Promotion> promotionsTable = new TableView<>();
        TableColumn<Promotion, String> codeColumn = new TableColumn<>("Code");
        TableColumn<Promotion, Double> discountColumn = new TableColumn<>("Discount (%)");
        TableColumn<Promotion, String> validFromColumn = new TableColumn<>("Valid From");
        TableColumn<Promotion, String> validToColumn = new TableColumn<>("Valid To");

        codeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCode()));
        discountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDiscount()).asObject());
        validFromColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValidFrom().toString()));
        validToColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValidTo().toString()));

        promotionsTable.getColumns().addAll(codeColumn, discountColumn, validFromColumn, validToColumn);

        ObservableList<Promotion> promotionsList = FXCollections.observableArrayList(DbConnection.getAllPromotions());
        promotionsTable.setItems(promotionsList);

        Button addPromotionButton = new Button("Add Promotion");
        addPromotionButton.setOnAction(event -> showAddPromotionForm(promotionsList));

        promotionsView.getChildren().addAll(titleLabel, promotionsTable, addPromotionButton);

        return promotionsView;
    }

    private void showAddPromotionForm(ObservableList<Promotion> promotionsList) {
        Dialog<Promotion> promotionDialog = new Dialog<>();
        promotionDialog.setTitle("Add New Promotion");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        TextField codeField = new TextField();
        codeField.setPromptText("Promotion Code");

        TextField discountField = new TextField();
        discountField.setPromptText("Discount Percentage");

        DatePicker validFromPicker = new DatePicker();
        validFromPicker.setPromptText("Valid From");

        DatePicker validToPicker = new DatePicker();
        validToPicker.setPromptText("Valid To");

        grid.add(new Label("Promotion Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Discount (%):"), 0, 1);
        grid.add(discountField, 1, 1);
        grid.add(new Label("Valid From:"), 0, 2);
        grid.add(validFromPicker, 1, 2);
        grid.add(new Label("Valid To:"), 0, 3);
        grid.add(validToPicker, 1, 3);

        promotionDialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType("Add Promotion", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        promotionDialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        promotionDialog.setResultConverter(button -> {
            if (button == okButtonType) {
                String code = codeField.getText().trim();
                String discountInput = discountField.getText().trim();
                LocalDate validFrom = validFromPicker.getValue();
                LocalDate validTo = validToPicker.getValue();

                if (code.isEmpty() || discountInput.isEmpty() || validFrom == null || validTo == null) {
                    showAlert("Error", "Invalid Input", "Please fill in all fields.");
                    return null;
                }

                double discount;
                try {
                    discount = Double.parseDouble(discountInput);
                    if (discount < 0 || discount > 100) {
                        showAlert("Error", "Invalid Discount", "Please enter a discount between 0 and 100.");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    showAlert("Error", "Invalid Discount", "Please enter a valid number for the discount.");
                    return null;
                }

                Promotion newPromotion = new Promotion(0, code, discount, validFrom, validTo);

                boolean success = DbConnection.addPromotion(newPromotion);
                if (success) {
                    promotionsList.add(newPromotion);
                    showAlert("Success", "Promotion Added", "The promotion has been added successfully.");
                    return newPromotion;
                } else {
                    showAlert("Error", "Unable to Add Promotion", "There was an error adding the promotion.");
                    return null;
                }
            }
            return null;
        });

        promotionDialog.showAndWait();
    }

    private void showAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
