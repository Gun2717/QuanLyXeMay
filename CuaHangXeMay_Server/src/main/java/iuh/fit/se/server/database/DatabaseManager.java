package iuh.fit.se.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;

public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class);

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/motorcycle_shop";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            logger.info("Database connection established successfully");
        } catch (ClassNotFoundException e) {
            logger.error("MariaDB JDBC Driver not found", e);
            throw new RuntimeException("Database driver not found", e);
        } catch (SQLException e) {
            logger.error("Failed to connect to database", e);
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                logger.info("Database connection re-established");
            }
        } catch (SQLException e) {
            logger.error("Failed to get database connection", e);
            throw new RuntimeException("Failed to get database connection", e);
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Failed to close database connection", e);
        }
    }

    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            logger.error("Error testing database connection", e);
            return false;
        }
    }
}
