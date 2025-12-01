package com.gymflow.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton class for managing database connections.
 * Ensures only one connection instance is used throughout the application.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private final String url;
    private final String username;
    private final String password;

    private DatabaseConnection() {
        Properties props = DatabaseConfig.load();
        this.url = props.getProperty("url");
        this.username = props.getProperty("username");
        this.password = props.getProperty("password");
    }

    /**
     * Gets the singleton instance of DatabaseConnection.
     *
     * @return the DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Resets the singleton instance. Useful for testing.
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            try {
                instance.closeConnection();
            } catch (Exception e) {
                // Ignore errors when closing
            }
        }
        instance = null;
    }

    /**
     * Gets a database connection. Creates a new connection if one doesn't exist
     * or if the existing connection is closed.
     *
     * @return a Connection to the database
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }

    /**
     * Closes the database connection.
     *
     * @throws SQLException if a database access error occurs
     */
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

