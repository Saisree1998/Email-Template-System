package servlet;

import java.sql.*;

public class DatabaseUtil {

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/email_system";
    private static final String USER = "root";
    private static final String PASS = "Sivasai@20";

    // To establish a database connection
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL JDBC driver
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // Check if a user exists
    public static boolean userExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // If count is greater than 0, user exists
                }
            }
        }
        return false;
    }

    // Method to register a user
    public static boolean registerUser(String username, String email, String password) {
        try {
            if (userExists(username)) {
                return false; // User already exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Registration failed due to an error
        }

        String query = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password); 
            
            return stmt.executeUpdate() > 0; // Return true if user was inserted
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Registration failed
        }
    }

    // Save OAuth tokens
    public static void saveOAuthTokens(String userId, String accessToken, String refreshToken, Timestamp tokenExpiry) throws SQLException {
        String query = "INSERT INTO oauth_tokens (user_id, access_token, refresh_token, token_expiry) VALUES (?, ?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE access_token = VALUES(access_token), refresh_token = VALUES(refresh_token), token_expiry = VALUES(token_expiry)";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userId);
            stmt.setString(2, accessToken);
            stmt.setString(3, refreshToken);
            stmt.setTimestamp(4, tokenExpiry);
            stmt.executeUpdate();
        }
    }

    // Get OAuth tokens
    public static OAuthTokens getOAuthTokens(String userId) throws SQLException {
        String query = "SELECT access_token, refresh_token, token_expiry FROM oauth_tokens WHERE user_id = ?";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new OAuthTokens(
                            rs.getString("access_token"),
                            rs.getString("refresh_token"),
                            rs.getTimestamp("token_expiry")
                    );
                }
            }
        }
        return null;
    }

    // Test connection method
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Database connected successfully!");
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}

// OAuthTokens class to store OAuth token details
class OAuthTokens {
    private String accessToken;
    private String refreshToken;
    private Timestamp tokenExpiry;

    public OAuthTokens(String accessToken, String refreshToken, Timestamp tokenExpiry) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiry = tokenExpiry;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Timestamp getTokenExpiry() {
        return tokenExpiry;
    }
}
