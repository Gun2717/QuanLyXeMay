package iuh.fit.se.server;

import iuh.fit.se.server.database.DatabaseManager;
import iuh.fit.se.server.network.SocketServer;
import org.apache.log4j.Logger;

public class ServerMain {
    private static final Logger logger = Logger.getLogger(ServerMain.class);

    public static void main(String[] args) {
        logger.info("Starting Motorcycle Shop Server...");

        try {
            // Test database connection
            DatabaseManager dbManager = DatabaseManager.getInstance();
            if (dbManager.testConnection()) {
                logger.info("Database connection successful");
                System.out.println("Database connected successfully!");
            } else {
                logger.error("Database connection failed");
                System.err.println("Cannot connect to database. Please check your configuration.");
                return;
            }

            // Start socket server
            SocketServer server = new SocketServer();

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down server...");
                server.stop();
                dbManager.closeConnection();
                logger.info("Server shut down complete");
            }));

            // Start server
            server.start();

        } catch (Exception e) {
            logger.error("Fatal error starting server", e);
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
