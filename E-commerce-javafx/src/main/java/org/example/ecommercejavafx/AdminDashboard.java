package org.example.ecommercejavafx;

import com.itextpdf.layout.properties.TextAlignment;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Order;
import models.Product;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.example.ecommercejavafx.admin.ManageOrders;
import org.example.ecommercejavafx.admin.ManageProducts;
import org.example.ecommercejavafx.admin.ManageUsers;
import org.example.ecommercejavafx.admin.ManagePromotions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AdminDashboard extends Application {
    private String username;

    public AdminDashboard(String username) {
        this.username = username;
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        ToolBar toolBar = new ToolBar();
        toolBar.setStyle("-fx-background-color: #00b4d8;");

        Button manageUsersButton = createNavButton("Manage Users");
        Button manageProductsButton = createNavButton("Manage Products");
        Button viewOrdersButton = createNavButton("View Orders");
        Button generateReportsButton = createNavButton("Generate Reports");
        Button managePromotionsButton = createNavButton("Manage Promotions");
        Button logoutButton = createNavButton("Logout");

        toolBar.getItems().addAll(manageUsersButton, manageProductsButton, viewOrdersButton, generateReportsButton, managePromotionsButton, logoutButton);

        root.setTop(toolBar);

        VBox placeholder = new VBox();
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setSpacing(20);
        placeholder.getChildren().add(new Label("Welcome to the Admin Dashboard, " + username + "!"));
        placeholder.getChildren().add(new Label("What would you like to do today?"));
        root.setCenter(placeholder);


        manageUsersButton.setOnAction(event -> openModule(root, new ManageUsers(), "Users"));
        manageProductsButton.setOnAction(event -> openModule(root, new ManageProducts(), "Products"));
        viewOrdersButton.setOnAction(event -> openModule(root, new ManageOrders(), "Orders"));
        generateReportsButton.setOnAction(event -> generateReports());
        managePromotionsButton.setOnAction(event -> openModule(root, new ManagePromotions(), "Promotions"));
        logoutButton.setOnAction(event -> confirmLogout(primaryStage));


        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Dashboard");
        primaryStage.show();
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #0077b6; -fx-text-fill: white; -fx-font-size: 14px;");
        button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: #03045e; -fx-text-fill: white; -fx-font-size: 14px;"));
        button.setOnMouseExited(event -> button.setStyle("-fx-background-color: #0077b6; -fx-text-fill: white; -fx-font-size: 14px;"));
        return button;
    }

    private void openModule(BorderPane root, Object module, String moduleName) {
        VBox moduleView = null;

        if (module instanceof ManageUsers) {
            moduleView = ((ManageUsers) module).showManageUsersWindow();
        } else if (module instanceof ManageProducts) {
            moduleView = ((ManageProducts) module).showManageProductsWindow();
        } else if (module instanceof ManageOrders) {
            moduleView = ((ManageOrders) module).showViewOrdersWindow();
        } else if (module instanceof ManagePromotions) {
            moduleView = ((ManagePromotions) module).showManagePromotionsWindow();
        }

        if (moduleView != null) {
            root.setCenter(moduleView);
        } else {
            showAlert("Error", "Unable to load " + moduleName, "Please try again.");
        }
    }

    private void generateReports() {
        try {
            generateProductStockReport();
            generateOrderHistoryReport();
            showAlert("Generate Reports", "Reports Generated Successfully", "Product Stock and Order History reports have been generated and saved.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Report Generation Failed", "An error occurred while generating reports. Please try again.");
        }
    }

    public void generateProductStockReport() throws IOException {
        String dest = "Product_Stock_Report_Admin.pdf";
        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);


        Paragraph title = new Paragraph("Product Stock Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18);
        document.add(title);


        Table table = new Table(2);
        table.addHeaderCell(new Cell().add(new Paragraph("Product Name")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Cell().add(new Paragraph("Stock")).setBackgroundColor(ColorConstants.LIGHT_GRAY));


        List<Product> products = fetchProducts();

        for (Product product : products) {
            table.addCell(product.getName());
            table.addCell(String.valueOf(product.getStock()));
        }

        document.add(table);
        document.close();

        System.out.println("Product Stock Report saved.");
    }



    public void generateOrderHistoryReport() throws IOException {
        String destination = "Order_History_Report_Admin.pdf";
        PdfWriter writer = new PdfWriter(destination);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Paragraph title = new Paragraph("Order History Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18);
        document.add(title);

        Table table = new Table(3);
        table.addHeaderCell(new Cell().add(new Paragraph("Order ID")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Cell().add(new Paragraph("Total")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Cell().add(new Paragraph("Status")).setBackgroundColor(ColorConstants.LIGHT_GRAY));

        List<Order> orders = getOrders();

        for (Order order : orders) {
            table.addCell(String.valueOf(order.getId()));
            table.addCell("$" + order.getTotal());
            table.addCell(order.getStatus());
        }

        document.add(table);
        document.close();

        System.out.println("Order History Report saved.");
    }


    private void confirmLogout(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("Unsaved changes will be lost.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                primaryStage.close();
                LoginPage loginPage = new LoginPage();
                Stage loginStage = new Stage();
                loginPage.start(loginStage);
            }
        });
    }

    private List<Order> getOrders(){
            try (Connection connection = DbConnection.getConnection()){
                return DbConnection.fetchOrders();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

    }

    private List<Product> fetchProducts(){
        try (Connection connection = DbConnection.getConnection()){
            return DbConnection.fetchProducts();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    private void showAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
