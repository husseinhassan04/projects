package org.example.ecommercejavafx;

import models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbConnection {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/ecommerce";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Zouz@5201314";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    public static User authenticateUser(String username, String password) {
        try (Connection connection = getConnection()) {
            String query = "SELECT user_id, username, role, email, address, payment_method FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("user_id");
                String userRole = resultSet.getString("role");
                String email = resultSet.getString("email");
                String address = resultSet.getString("address");
                String paymentMethod = resultSet.getString("payment_method");

                return new User(id, username, userRole, email, address, paymentMethod);
            }
        } catch (SQLException e) {
            System.err.println("Error during authentication: " + e.getMessage());
        }
        return null;
    }

    public static User getUserInfo(int userId) {
        String query = "SELECT username, role, email, address, payment_method FROM users WHERE user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new User(
                        userId,
                        resultSet.getString("username"),
                        resultSet.getString("role"),
                        resultSet.getString("email"),
                        resultSet.getString("address"),
                        resultSet.getString("payment_method")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean registerUser(String username, String password, String email, String address, String paymentMethod) {
        try (Connection connection = getConnection()) {
            String query = "INSERT INTO users (username, password, email, address, payment_method, role) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, address);
            stmt.setString(5, paymentMethod);
            stmt.setString(6, "customer");
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (Exception e) {
            System.err.println("Error during user registration: " + e.getMessage());
            return false;
        }
    }



    public static List<Product> fetchProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products";
        try (Connection connection = DbConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("product_id");
                String name = resultSet.getString("name");
                String desc = resultSet.getString("description");
                double price = resultSet.getDouble("price");
                int stock = resultSet.getInt("stock");
                String imageUrl = resultSet.getString("image_url");

                products.add(new Product(id, name,desc, price, stock, imageUrl));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    public static List<Order> fetchOrders() {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DbConnection.getConnection()) {
            String query = """
                SELECT o.order_id, o.status, o.total, u.username AS customer_name, o.tracking_number
                FROM orders o
                LEFT JOIN users u ON o.user_id = u.user_id
            """;
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    int orderId = resultSet.getInt("order_id");
                    String status = resultSet.getString("status");
                    double total = resultSet.getDouble("total");
                    String trackingNb = resultSet.getString("tracking_number");
                    orders.add(new Order(orderId, status, total, trackingNb));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static void addProductToDatabase(String name, String description, double price, int stock, String imageUrl) {
        String query = "INSERT INTO products (name, description, price, stock, image_url) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setDouble(3, price);
            statement.setInt(4, stock);
            statement.setString(5, imageUrl);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeProduct(int productId) {
        String query = "DELETE FROM products WHERE product_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, productId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> fetchReviewsForProduct(int productId) {
        List<String> reviews = new ArrayList<>();
        String query = "SELECT CONCAT(u.username, ' - Rating: ', r.rating, '\n', r.review_text) AS review " +
                "FROM reviews r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "WHERE r.product_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reviews.add(rs.getString("review"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public static void updateOrderStatus(int orderId, String status) {
        String query = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, status);
            statement.setInt(2, orderId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void updateUserProfile(int userId, User user) {
        try (Connection connection = getConnection()) {
            String query = "UPDATE users SET username = ?, email = ?, address = ?, payment_method = ? WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getAddress());
            stmt.setString(4, user.getPaymentMethod());
            stmt.setInt(5, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error updating user profile: " + e.getMessage());
        }
    }




    public static void updateUserPassword(int userId, String newPassword) {

        String query = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);

            stmt.executeUpdate();
            System.out.println("Password updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Review> getReviews(int productId) {
        List<Review> reviews = new ArrayList<>();
        String query = """
            SELECT r.review_id, r.product_id, u.username, r.rating, r.review_text, r.review_date
            FROM reviews r
            JOIN users u ON r.user_id = u.user_id
            WHERE r.product_id = ?
            ORDER BY r.review_date DESC
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, productId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                reviews.add(new Review(
                        resultSet.getInt("review_id"),
                        resultSet.getInt("product_id"),
                        resultSet.getString("username"),
                        resultSet.getInt("rating"),
                        resultSet.getString("review_text"),
                        resultSet.getTimestamp("review_date").toLocalDateTime()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public static boolean addReview(int productId, int userId, int rating, String reviewText) {
        String query = """
            INSERT INTO reviews (product_id, user_id, rating, review_text)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, productId);
            statement.setInt(2, userId);
            statement.setInt(3, rating);
            statement.setString(4, reviewText);
            statement.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<User> fetchUsers() {
        List<User> users = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM users";
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    int id = resultSet.getInt("user_id");
                    String username = resultSet.getString("username");
                    String role = resultSet.getString("role");
                    String email = resultSet.getString("email");
                    String address = resultSet.getString("address");
                    String paymentMethod = resultSet.getString("payment_method");
                    users.add(new User(id, username, role, email, address, paymentMethod));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static void addUserToDatabase(String username, String password, String role, String email, String address, String paymentMethod) {
        try (Connection connection = getConnection()) {
            String query = "INSERT INTO users (username, password, role, email, address, payment_method) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, password);
                statement.setString(3, role);
                statement.setString(4, email);
                statement.setString(5, address);
                statement.setString(6, paymentMethod);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeUser(User user) {
        try (Connection connection = getConnection()) {
            String query = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, user.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet loadCartItems(Connection connection, int userId) throws SQLException {
        String query = "SELECT c.product_id, c.quantity, p.name, p.price, p.description, p.image_url, p.stock " +
                "FROM cart c INNER JOIN products p ON c.product_id = p.product_id WHERE c.user_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);
        return statement.executeQuery();
    }

    public static void saveCartItem(Connection connection, int userId, CartItem item) throws SQLException {
        String query = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, item.getProduct().getId());
            statement.setInt(3, item.getQuantity());
            statement.setInt(4, item.getQuantity());
            statement.executeUpdate();
        }
    }

    public static void removeCartItem(Connection connection, int userId, CartItem item) throws SQLException {
        String query = "DELETE FROM cart WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, item.getProduct().getId());
            statement.executeUpdate();
        }
    }

    public static void completeCheckout(Connection connection, int userId, double finalPrice, ShippingProvider provider, List<CartItem> cart) throws SQLException {
        String query = "INSERT INTO orders (user_id, total, shipping_provider,status, order_date) VALUES (?, ?, ?,?, NOW())";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setDouble(2, finalPrice);
            statement.setInt(3, provider.getId());
            statement.setString(4,"pending");
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int orderId = generatedKeys.getInt(1);

                for (CartItem item : cart) {
                    String insertItemQuery = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement itemStatement = connection.prepareStatement(insertItemQuery)) {
                        itemStatement.setInt(1, orderId);
                        itemStatement.setInt(2, item.getProduct().getId());
                        itemStatement.setInt(3, item.getQuantity());
                        itemStatement.executeUpdate();
                    }
                }
            }
        }
    }

    public static ArrayList<Order> getOrdersByUserId(int userId) throws SQLException {
        ArrayList<Order> orders = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String query = "SELECT order_id, status, tracking_number, total, estimated_delivery " +
                    "FROM orders WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Order order = new Order(
                        resultSet.getInt("order_id"),
                        resultSet.getString("status"),
                        resultSet.getDouble("total"),
                        resultSet.getString("tracking_number")
                );
                ArrayList<Product> products = getProductsForOrder(order.getId(), connection);
                order.setProducts(products);
                orders.add(order);
            }
        }
        return orders;
    }

    public static ArrayList<Product> getProductsForOrder(int orderId, Connection connection) throws SQLException {
        ArrayList<Product> products = new ArrayList<>();
        String query = "SELECT p.product_id, p.name, p.description, p.price, p.stock, p.image_url " +
                "FROM products p JOIN order_items oi ON p.product_id = oi.product_id " +
                "WHERE oi.order_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, orderId);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Product product = new Product(
                    resultSet.getInt("product_id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getDouble("price"),
                    resultSet.getInt("stock"),
                    resultSet.getString("image_url")
            );
            products.add(product);
        }
        return products;
    }

    public static void cancelOrder(int orderId) throws SQLException {
        try (Connection connection = getConnection()) {
            String query = "UPDATE orders SET status = 'Canceled' WHERE order_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, orderId);
            statement.executeUpdate();
        }
    }

    public static void clearCart(Connection connection, int userId) throws SQLException {
        String query = "DELETE FROM cart WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    public static double calculateAverageRating(int productId) {
        String query = "SELECT AVG(rating) FROM reviews WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean updateProductRating(int productId, double newRating) {
        String query = "UPDATE products SET rating = ? WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, newRating);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static List<Promotion> getAllPromotions() {
        List<Promotion> promotions = new ArrayList<>();
        String query = "SELECT * FROM promotions";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int promotionId = resultSet.getInt("promotion_id");
                String code = resultSet.getString("code");
                double discountPercentage = resultSet.getDouble("discount_percentage");
                Date validFromDate = resultSet.getDate("valid_from");
                Date validToDate = resultSet.getDate("valid_to");

                Promotion promotion = new Promotion(promotionId, code, discountPercentage,
                        validFromDate.toLocalDate(), validToDate.toLocalDate());
                promotions.add(promotion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return promotions;
    }

    public static boolean addPromotion(Promotion promotion) {
        String query = "INSERT INTO promotions (code, discount_percentage, valid_from, valid_to) VALUES (?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, promotion.getCode());
            preparedStatement.setDouble(2, promotion.getDiscountPercentage());
            preparedStatement.setDate(3, Date.valueOf(promotion.getValidFrom()));
            preparedStatement.setDate(4, Date.valueOf(promotion.getValidTo()));

            int result = preparedStatement.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Product> getProducts(String query) throws Exception {
        List<Product> products = new ArrayList<>();
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            String sql = "SELECT * FROM products";
            if (query != null && !query.isBlank()) {
                sql += " WHERE name LIKE '%" + query + "%'";
            }

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    Product product = new Product(
                            resultSet.getInt("product_id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getDouble("price"),
                            resultSet.getInt("stock"),
                            resultSet.getString("image_url")
                    );
                    products.add(product);
                }
            }
        }
        return products;
    }

    public static List<Product> getWishlist(int userId) {
        List<Product> wishlist = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String query = "SELECT p.product_id, p.name, p.description, p.price, p.stock, p.image_url " +
                    "FROM products p " +
                    "JOIN wishlist w ON p.product_id = w.product_id " +
                    "WHERE w.user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Product product = new Product(
                                resultSet.getInt("product_id"),
                                resultSet.getString("name"),
                                resultSet.getString("description"),
                                resultSet.getDouble("price"),
                                resultSet.getInt("stock"),
                                resultSet.getString("image_url")
                        );
                        wishlist.add(product);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wishlist;
    }

    public static boolean isProductInWishlist(int userId, Product product) {
        try (Connection connection = getConnection()) {
            String query = "SELECT 1 FROM wishlist WHERE user_id = ? AND product_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                statement.setInt(2, product.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void addToWishlist(int userId, Product product) {
        try (Connection connection = getConnection()) {
            String query = "INSERT INTO wishlist (user_id, product_id) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                statement.setInt(2, product.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeFromWishlist(int userId, Product product) {
        try (Connection connection = getConnection()) {
            String query = "DELETE FROM wishlist WHERE user_id = ? AND product_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                statement.setInt(2, product.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<ShippingProvider> getShippingProviders() {
        List<ShippingProvider> providers = new ArrayList<>();
        String query = "SELECT provider_id, name, price FROM shipping_providers";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                providers.add(new ShippingProvider(
                        resultSet.getInt("provider_id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return providers;
    }

    public static Promotion validatePromoCode(String promoCode) {
        String query = "SELECT * FROM promotions WHERE code = ? AND CURDATE() BETWEEN valid_from AND valid_to";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, promoCode);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new Promotion(
                        resultSet.getInt("promotion_id"),
                        resultSet.getString("code"),
                        resultSet.getDouble("discount_percentage"),
                        resultSet.getDate("valid_from").toLocalDate(),
                        resultSet.getDate("valid_to").toLocalDate()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}

