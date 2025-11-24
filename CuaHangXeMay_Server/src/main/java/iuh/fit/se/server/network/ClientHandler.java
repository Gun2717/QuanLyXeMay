package iuh.fit.se.server.network;

import iuh.fit.se.common.*;
import iuh.fit.se.server.service.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class);

    private Socket socket;
    private SocketServer server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private volatile boolean isConnected;

    private UserService userService;
    private ProductService productService;
    private CustomerService customerService;
    private OrderService orderService;
    private InventoryService inventoryService;

    public ClientHandler(Socket socket, SocketServer server) {
        this.socket = socket;
        this.server = server;
        this.isConnected = true;

        // Initialize services
        this.userService = new UserService();
        this.productService = new ProductService();
        this.customerService = new CustomerService();
        this.orderService = new OrderService();
        this.inventoryService = new InventoryService();

        try {
            // Set socket options
            socket.setKeepAlive(true);
            socket.setSoTimeout(300000); // 5 minutes timeout

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            logger.info("Client handler initialized for: " + socket.getInetAddress());
        } catch (IOException e) {
            logger.error("Error creating streams", e);
            disconnect();
        }
    }

    @Override
    public void run() {
        try {
            while (isConnected && !socket.isClosed()) {
                try {
                    Request request = (Request) in.readObject();

                    if (request == null) {
                        logger.warn("Received null request");
                        continue;
                    }

                    logger.info("Received request: " + request.getAction() + " from " + socket.getInetAddress());

                    Response response = processRequest(request);

                    synchronized (out) {
                        out.reset(); // Clear object cache
                        out.writeObject(response);
                        out.flush();
                    }

                } catch (EOFException e) {
                    logger.info("Client disconnected normally: " + socket.getInetAddress());
                    break;
                } catch (SocketException e) {
                    if (isConnected) {
                        logger.warn("Socket exception: " + e.getMessage());
                    }
                    break;
                } catch (Exception e) {
                    logger.error("Error processing request", e);
                    try {
                        synchronized (out) {
                            out.reset();
                            out.writeObject(Response.error("Server error: " + e.getMessage()));
                            out.flush();
                        }
                    } catch (IOException ex) {
                        logger.error("Error sending error response", ex);
                        break;
                    }
                }
            }
        } finally {
            disconnect();
        }
    }

    private Response processRequest(Request request) {
        String action = request.getAction();

        try {
            switch (action) {
                // Authentication
                case Constants.LOGIN:
                    return userService.login(request);

                // User Management
                case Constants.GET_ALL_USERS:
                    return userService.getAllUsers();
                case Constants.GET_USER_BY_ID:
                    return userService.getUserById(request);
                case Constants.CREATE_USER:
                    return userService.createUser(request);
                case Constants.UPDATE_USER:
                    return userService.updateUser(request);
                case Constants.DELETE_USER:
                    return userService.deleteUser(request);

                // Product Management
                case Constants.GET_ALL_PRODUCTS:
                    return productService.getAllProducts();
                case Constants.GET_PRODUCT_BY_ID:
                    return productService.getProductById(request);
                case Constants.CREATE_PRODUCT:
                    return productService.createProduct(request);
                case Constants.UPDATE_PRODUCT:
                    return productService.updateProduct(request);
                case Constants.DELETE_PRODUCT:
                    return productService.deleteProduct(request);
                case Constants.SEARCH_PRODUCTS:
                    return productService.searchProducts(request);
                case Constants.GET_PRODUCTS_BY_CATEGORY:
                    return productService.getProductsByCategory(request);

                // Category Management
                case Constants.GET_ALL_CATEGORIES:
                    return productService.getAllCategories();
                case Constants.CREATE_CATEGORY:
                    return productService.createCategory(request);

                // Customer Management
                case Constants.GET_ALL_CUSTOMERS:
                    return customerService.getAllCustomers();
                case Constants.GET_CUSTOMER_BY_ID:
                    return customerService.getCustomerById(request);
                case Constants.CREATE_CUSTOMER:
                    return customerService.createCustomer(request);
                case Constants.UPDATE_CUSTOMER:
                    return customerService.updateCustomer(request);
                case Constants.DELETE_CUSTOMER:
                    return customerService.deleteCustomer(request);
                case Constants.SEARCH_CUSTOMERS:
                    return customerService.searchCustomers(request);

                // Order Management
                case Constants.GET_ALL_ORDERS:
                    return orderService.getAllOrders();
                case Constants.GET_ORDER_BY_ID:
                    return orderService.getOrderById(request);
                case Constants.CREATE_ORDER:
                    return orderService.createOrder(request);
                case Constants.UPDATE_ORDER_STATUS:
                    return orderService.updateOrderStatus(request);
                case Constants.GET_ORDER_ITEMS:
                    return orderService.getOrderItems(request);

                // Inventory Management
                case Constants.GET_ALL_INVENTORY:
                    return inventoryService.getAllInventory();
                case Constants.UPDATE_INVENTORY:
                    return inventoryService.updateInventory(request);
                case Constants.GET_LOW_STOCK_PRODUCTS:
                    return inventoryService.getLowStockProducts();

                default:
                    logger.warn("Unknown action: " + action);
                    return Response.error("Unknown action: " + action);
            }
        } catch (Exception e) {
            logger.error("Error processing request: " + action, e);
            return Response.error("Server error: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (!isConnected) {
            return;
        }

        isConnected = false;

        try {
            if (in != null) {
                try { in.close(); } catch (Exception e) {}
            }
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (Exception e) {}
            }
            if (socket != null && !socket.isClosed()) {
                try { socket.close(); } catch (Exception e) {}
            }

            server.removeClient(this);
            logger.info("Client disconnected: " + (socket != null ? socket.getInetAddress() : "unknown"));

        } catch (Exception e) {
            logger.error("Error disconnecting client", e);
        }
    }

    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }
}
