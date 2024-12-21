package org.example.ecommercejavafx;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.User;
import org.example.ecommercejavafx.customer.CartService;
import org.example.ecommercejavafx.customer.EditProfileView;
import org.example.ecommercejavafx.customer.OrdersView;
import org.example.ecommercejavafx.customer.ProductService;
import org.example.ecommercejavafx.customer.WishlistView;


public class CustomerDashboard extends Application {

    private WishlistView wishlistView;
    private User user;

    @Override
    public void start(Stage primaryStage) {
        wishlistView = new WishlistView(user.getId());

        BorderPane root = new BorderPane();

        ToolBar toolBar = new ToolBar();
        toolBar.setStyle("-fx-background-color: #00b4d8;");

        Button browseProductsButton = createNavButton("Browse Products");
        Button viewCartButton = createNavButton("View Cart");
        Button viewWishlistButton = createNavButton("View Wishlist");
        Button viewOrdersButton = createNavButton("View Orders");
        Button editProfileButton = createNavButton("Edit Profile");
        Button logoutButton = createNavButton("Logout");

        toolBar.getItems().addAll(browseProductsButton, viewCartButton, viewWishlistButton, viewOrdersButton, editProfileButton, logoutButton);

        root.setTop(toolBar);


        VBox placeholder = new VBox();
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setSpacing(20);
        placeholder.getChildren().add(new Label("Welcome, " + user.getUsername() + "!"));
        placeholder.getChildren().add(new Label("What would you like to do today?"));
        root.setCenter(placeholder);

        browseProductsButton.setOnAction(event -> openBrowseProducts(root));
        viewCartButton.setOnAction(event -> viewCart(root));
        viewWishlistButton.setOnAction(event -> viewWishlist(root));
        viewOrdersButton.setOnAction(event -> viewOrders(root));
        editProfileButton.setOnAction(event -> editProfile());
        logoutButton.setOnAction(event -> logout(primaryStage));

        Scene scene = new Scene(root, 1000, 750);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Customer Dashboard");
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
        ProductService productService = new ProductService( wishlistView, user.getId());
        VBox browseProductsView = productService.openBrowseProducts();
        root.setCenter(browseProductsView);
    }

    private void viewCart(BorderPane root) {
        CartService cartService = new CartService( user.getId());
        VBox cartView = cartService.viewCart();
        root.setCenter(cartView);
    }

    private void viewWishlist(BorderPane root) {
        wishlistView.refreshWishlist();
        root.setCenter(wishlistView);
    }

    private void viewOrders(BorderPane root) {
        OrdersView ordersView = new OrdersView(user.getId());
        root.setCenter(ordersView);
    }

    private void editProfile() {
        EditProfileView editProfileView = new EditProfileView(user.getId(), user);
        Stage editStage = new Stage();
        editProfileView.start(editStage);
    }

    private void logout(Stage primaryStage) {
        System.out.println("User logged out successfully.");
        primaryStage.close();
    }

    public CustomerDashboard(User user) {
        this.user = user;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
