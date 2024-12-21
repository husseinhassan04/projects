package org.example.ecommercejavafx;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.ecommercejavafx.customer.ProductService;

public class GuestDashboard extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        ToolBar toolBar = new ToolBar();
        toolBar.setStyle("-fx-background-color: #00b4d8;");

        Button browseProductsButton = createNavButton("Browse Products");
        Button loginButton = createNavButton("Login");

        toolBar.getItems().addAll(browseProductsButton, loginButton);
        root.setTop(toolBar);

        VBox placeholder = new VBox();
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setSpacing(20);
        placeholder.getChildren().add(new Label("Welcome to E-Commerce"));
        placeholder.getChildren().add(new Label("Please browse products or log in for more features."));
        root.setCenter(placeholder);

        browseProductsButton.setOnAction(event -> openBrowseProducts(root));
        loginButton.setOnAction(event -> openLoginPage(primaryStage));

        Scene scene = new Scene(root, 1000, 750);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Guest Dashboard");
        primaryStage.show();
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #0077b6; -fx-text-fill: white; -fx-font-size: 14px;");
        button.setOnMouseEntered(event -> button.setStyle("-fx-background-color: #03045e; -fx-text-fill: white; -fx-font-size: 14px;"));
        button.setOnMouseExited(event -> button.setStyle("-fx-background-color: #0077b6; -fx-text-fill: white; -fx-font-size: 14px;"));
        return button;
    }

    private void openBrowseProducts(BorderPane root) {
        ProductService productService = new ProductService(null, -1);
        VBox browseProductsView = productService.openBrowseProducts();
        root.setCenter(browseProductsView);
    }

    private void openLoginPage(Stage primaryStage) {
        LoginPage loginPage = new LoginPage();
        loginPage.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
