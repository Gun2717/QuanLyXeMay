package iuh.fit.se.server.network;

import iuh.fit.se.common.Constants;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private static final Logger logger = Logger.getLogger(SocketServer.class);

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private List<ClientHandler> clients;
    private boolean isRunning;

    public SocketServer() {
        this.threadPool = Executors.newCachedThreadPool();
        this.clients = new ArrayList<>();
        this.isRunning = false;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            isRunning = true;
            logger.info("Server started on port " + Constants.SERVER_PORT);
            System.out.println("=================================");
            System.out.println("Server is running on port " + Constants.SERVER_PORT);
            System.out.println("Waiting for clients...");
            System.out.println("=================================");

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connected: " + clientSocket.getInetAddress());
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            if (isRunning) {
                logger.error("Server error", e);
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            for (ClientHandler client : clients) {
                client.disconnect();
            }
            clients.clear();

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            threadPool.shutdown();
            logger.info("Server stopped");
            System.out.println("Server stopped");
        } catch (IOException e) {
            logger.error("Error stopping server", e);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        logger.info("Client removed. Total clients: " + clients.size());
    }

    public List<ClientHandler> getClients() {
        return clients;
    }
}
