package org.example.ecommercejavafx.customer;

import com.itextpdf.layout.properties.TextAlignment;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import models.Order;
import models.Product;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.example.ecommercejavafx.DbConnection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdersView extends VBox {
    private final int userId;
    private ListView<Order> ordersListView;

    public OrdersView(int userId) {
        this.userId = userId;
        this.ordersListView = new ListView<>();

        refreshOrderList();

        Button generateReportButton = new Button("Generate PDF Report");
        generateReportButton.setOnAction(e -> generatePdfReport());

        ScrollPane scrollPane = new ScrollPane(ordersListView);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-border-color: transparent;");

        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            double buttonHeight = generateReportButton.getHeight();
            double paddingHeight = this.getPadding().getTop() + this.getPadding().getBottom();
            ordersListView.setPrefHeight(newValue.doubleValue() - buttonHeight - paddingHeight - 10);
        });

        this.getChildren().addAll(scrollPane, generateReportButton);
        this.setSpacing(10);
        this.setStyle("-fx-padding: 10;");
    }

    private void refreshOrderList() {
        ordersListView.getItems().clear();
        try {
            ordersListView.getItems().addAll(DbConnection.getOrdersByUserId(userId));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ordersListView.setCellFactory(param -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);

                if (empty || order == null) {
                    setGraphic(null);
                    return;
                }

                VBox vBox = new VBox();
                vBox.setSpacing(15);
                vBox.setStyle("-fx-padding: 15; -fx-background-color: #f4f4f4; -fx-border-radius: 10px; -fx-background-radius: 10px;");

                vBox.getChildren().add(new Text("Total: $" + order.getTotal()));
                vBox.getChildren().add(new Text("Shipping Status: " + order.getStatus()));
                vBox.getChildren().add(new Text("Tracking Number: " + order.getTrackingNumber()));

                VBox productBox = new VBox();
                productBox.setSpacing(10);
                productBox.getChildren().add(new Text("Products:"));

                for (Product product : order.getProducts()) {
                    HBox productHBox = new HBox();
                    productHBox.setSpacing(15);
                    productHBox.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-radius: 10px;");

                    ImageView productImageView = new ImageView(new Image(product.getImageUrl()));
                    productImageView.setFitWidth(80);
                    productImageView.setFitHeight(80);
                    productHBox.getChildren().add(productImageView);

                    VBox productDetailsBox = new VBox();
                    productDetailsBox.setSpacing(5);
                    productDetailsBox.getChildren().add(new Text("Name: " + product.getName()));
                    productDetailsBox.getChildren().add(new Text("Price: $" + product.getPrice()));
                    productHBox.getChildren().add(productDetailsBox);

                    productBox.getChildren().add(productHBox);
                }

                vBox.getChildren().add(productBox);

                if ("Pending".equalsIgnoreCase(order.getStatus())) {
                    Button cancelButton = new Button("Cancel Order");
                    cancelButton.setOnAction(e -> {
                        try {
                            DbConnection.cancelOrder(order.getId());
                            refreshOrderList();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });
                    vBox.getChildren().add(cancelButton);
                }

                setGraphic(vBox);
            }
        });
    }

    public void generatePdfReport() {
        String fileName = "OrderHistory.pdf";

        try {
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            Paragraph title = new Paragraph("Order Report")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            document.add(title);

            for (Order order : ordersListView.getItems()) {
                Paragraph orderIdParagraph = new Paragraph("Order ID: " + order.getId())
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontSize(12);
                document.add(orderIdParagraph);

                Paragraph totalParagraph = new Paragraph("Total: $" + order.getTotal())
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontSize(12);
                document.add(totalParagraph);

                Paragraph statusParagraph = new Paragraph("Status: " + order.getStatus())
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontSize(12);
                document.add(statusParagraph);

                Paragraph trackingNumberParagraph = new Paragraph("Tracking Number: " + order.getTrackingNumber())
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontSize(12);
                document.add(trackingNumberParagraph);

                com.itextpdf.layout.element.List productList = new com.itextpdf.layout.element.List()
                        .setListSymbol("•")
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.LEFT);

                for (Product product : order.getProducts()) {
                    productList.add("Product: " + product.getName() + ", Price: $" + product.getPrice());
                }

                document.add(productList);
                document.add(new Paragraph("\n"));
            }

            document.close();
            System.out.println("Report generated: " + fileName);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
