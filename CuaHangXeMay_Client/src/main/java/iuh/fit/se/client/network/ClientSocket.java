package iuh.fit.se.client.network;

import iuh.fit.se.client.config.ServerConfig;
import iuh.fit.se.common.Request;
import iuh.fit.se.common.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSocket {
    private static ClientSocket instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isConnected;

    // Add synchronization lock
    private final Object lock = new Object();

    private ClientSocket() {
        this.isConnected = false;
    }

    public static synchronized ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    public boolean connect() {
        synchronized (lock) {
            try {
                if (socket != null && !socket.isClosed() && isConnected) {
                    return true; // Already connected
                }

                socket = new Socket(ServerConfig.getHost(), ServerConfig.getPort());
                socket.setKeepAlive(true);
                socket.setSoTimeout(30000); // 30 seconds timeout

                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                isConnected = true;
                System.out.println("Connected to server: " + ServerConfig.getServerAddress());
                return true;
            } catch (IOException e) {
                System.err.println("Cannot connect to server: " + e.getMessage());
                isConnected = false;
                return false;
            }
        }
    }

    public Response sendRequest(Request request) {
        synchronized (lock) {
            if (!isConnected || socket == null || socket.isClosed()) {
                if (!connect()) {
                    return Response.error("Không thể kết nối đến server");
                }
            }

            try {
                // Reset streams if needed
                out.reset();
                out.writeObject(request);
                out.flush();

                Response response = (Response) in.readObject();
                return response;

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error sending request: " + e.getMessage());
                e.printStackTrace();
                disconnect();

                // Try to reconnect once
                if (connect()) {
                    try {
                        out.reset();
                        out.writeObject(request);
                        out.flush();
                        return (Response) in.readObject();
                    } catch (Exception ex) {
                        return Response.error("Lỗi kết nối: " + ex.getMessage());
                    }
                }

                return Response.error("Lỗi kết nối: " + e.getMessage());
            }
        }
    }

    public void disconnect() {
        synchronized (lock) {
            try {
                isConnected = false;
                if (in != null) {
                    try { in.close(); } catch (Exception e) {}
                }
                if (out != null) {
                    try { out.close(); } catch (Exception e) {}
                }
                if (socket != null && !socket.isClosed()) {
                    try { socket.close(); } catch (Exception e) {}
                }
                System.out.println("Disconnected from server");
            } catch (Exception e) {
                System.err.println("Error disconnecting: " + e.getMessage());
            } finally {
                socket = null;
                in = null;
                out = null;
            }
        }
    }

    public boolean isConnected() {
        synchronized (lock) {
            return isConnected && socket != null && !socket.isClosed();
        }
    }

    public void reconnect() {
        disconnect();
        try {
            Thread.sleep(500); // Wait a bit before reconnecting
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        connect();
    }
}
