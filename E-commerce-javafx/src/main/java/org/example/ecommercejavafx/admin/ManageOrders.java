package org.example.ecommercejavafx.admin;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.beans.property.ReadOnlyObjectWrapper;
import models.Order;
import org.example.ecommercejavafx.DbConnection;

import java.util.List;

public class ManageOrders {

    public VBox showViewOrdersWindow() {
        VBox layout = new VBox(10);
        layout.setPadding(new javafx.geometry.Insets(20));

        Label title = new Label("View Orders");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Order> orderTable = createOrderTable();
        List<Order> orders = DbConnection.fetchOrders();

        if (orders != null) {
            orderTable.getItems().addAll(orders);
        }

        layout.heightProperty().addListener((observable, oldValue, newValue) -> {
            double titleHeight = title.getHeight();
            double paddingHeight = layout.getPadding().getTop() + layout.getPadding().getBottom();
            orderTable.setPrefHeight(newValue.doubleValue() - titleHeight - paddingHeight);
        });

        layout.getChildren().addAll(title, orderTable);
        return layout;
    }

    private TableView<Order> createOrderTable() {
        TableView<Order> orderTable = new TableView<>();
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Order, Integer> orderIdColumn = new TableColumn<>("Order ID");
        orderIdColumn.setCellValueFactory(data -> data.getValue().idProperty().asObject());

        TableColumn<Order, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        TableColumn<Order, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(data -> data.getValue().totalProperty().asObject());

        TableColumn<Order, Order> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        actionColumn.setCellFactory(param -> createStatusCell());

        orderTable.getColumns().addAll(orderIdColumn, statusColumn, totalColumn, actionColumn);
        return orderTable;
    }

    private TableCell<Order, Order> createStatusCell() {
        return new TableCell<>() {
            private final ComboBox<String> statusComboBox = new ComboBox<>();
            private final Label statusLabel = new Label();

            {
                statusComboBox.getItems().addAll("Pending", "Processing", "Shipped", "Delivered");
                statusComboBox.setOnAction(event -> handleStatusChange());
            }

            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);
                if (order == null || empty) {
                    setGraphic(null);
                    return;
                }

                if ("Canceled".equalsIgnoreCase(order.getStatus())) {
                    statusLabel.setText(order.getStatus());
                    setGraphic(statusLabel);
                } else {
                    statusComboBox.setValue(order.getStatus());
                    setGraphic(statusComboBox);
                }
            }

            private void handleStatusChange() {
                Order order = getItem();
                if (order != null) {
                    String newStatus = statusComboBox.getValue();
                    DbConnection.updateOrderStatus(order.getId(), newStatus);
                    order.setStatus(newStatus);
                    getTableView().refresh();
                }
            }
        };
    }
}
