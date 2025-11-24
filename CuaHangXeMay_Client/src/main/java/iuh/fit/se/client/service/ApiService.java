package iuh.fit.se.client.service;

import iuh.fit.se.client.network.ClientSocket;
import iuh.fit.se.common.*;

import java.util.HashMap;
import java.util.Map;

public class ApiService {
    private ClientSocket clientSocket;

    public ApiService() {
        this.clientSocket = ClientSocket.getInstance();
    }

    // Authentication
    public Response login(String username, String password) {
        Request request = new Request(Constants.LOGIN);
        request.addData("username", username);
        request.addData("password", password);
        return clientSocket.sendRequest(request);
    }

    // User Management
    public Response getAllUsers() {
        return clientSocket.sendRequest(new Request(Constants.GET_ALL_USERS));
    }

    public Response getUserById(int id) {
        Request request = new Request(Constants.GET_USER_BY_ID);
        request.addData("id", id);
        return clientSocket.sendRequest(request);
    }

    public Response createUser(User user) {
        Request request = new Request(Constants.CREATE_USER);
        request.addData("user", user);
        return clientSocket.sendRequest(request);
    }

    public Response updateUser(User user) {
        Request request = new Request(Constants.UPDATE_USER);
        request.addData("user", user);
        return clientSocket.sendRequest(request);
    }

    public Response deleteUser(int id) {
        Request request = new Request(Constants.DELETE_USER);
        request.addData("id", id);
        return clientSocket.sendRequest(request);
    }

    // Product Management
    public Response getAllProducts() {
        return clientSocket.sendRequest(new Request(Constants.GET_ALL_PRODUCTS));
    }

    public Response getProductById(int id) {
        Request request = new Request(Constants.GET_PRODUCT_BY_ID);
        request.addData("id", id);
        return clientSocket.sendRequest(request);
    }

    public Response createProduct(Product product) {
        Request request = new Request(Constants.CREATE_PRODUCT);
        request.addData("product", product);
        return clientSocket.sendRequest(request);
    }

    public Response updateProduct(Product product) {
        Request request = new Request(Constants.UPDATE_PRODUCT);
        request.addData("product", product);
        return clientSocket.sendRequest(request);
    }

    public Response deleteProduct(int id) {
        Request request = new Request(Constants.DELETE_PRODUCT);
        request.addData("id", id);
        return clientSocket.sendRequest(request);
    }

    public Response searchProducts(String keyword) {
        Request request = new Request(Constants.SEARCH_PRODUCTS);
        request.addData("keyword", keyword);
        return clientSocket.sendRequest(request);
    }

    public Response getProductsByCategory(int categoryId) {
        Request request = new Request(Constants.GET_PRODUCTS_BY_CATEGORY);
        request.addData("categoryId", categoryId);
        return clientSocket.sendRequest(request);
    }

    // Category Management
    public Response getAllCategories() {
        return clientSocket.sendRequest(new Request(Constants.GET_ALL_CATEGORIES));
    }

    public Response createCategory(Category category) {
        Request request = new Request(Constants.CREATE_CATEGORY);
        request.addData("category", category);
        return clientSocket.sendRequest(request);
    }

    // Customer Management
    public Response getAllCustomers() {
        return clientSocket.sendRequest(new Request(Constants.GET_ALL_CUSTOMERS));
    }

    public Response getCustomerById(int id) {
        Request request = new Request(Constants.GET_CUSTOMER_BY_ID);
        request.addData("id", id);
        return clientSocket.sendRequest(request);
    }

    public Response createCustomer(Customer customer) {
        Request request = new Request(Constants.CREATE_CUSTOMER);
        request.addData("customer", customer);
        return clientSocket.sendRequest(request);
    }

    public Response updateCustomer(Customer customer) {
        Request request = new Request(Constants.UPDATE_CUSTOMER);
        request.addData("customer", customer);
        return clientSocket.sendRequest(request);
    }

    public Response deleteCustomer(int id) {
        Request request = new Request(Constants.DELETE_CUSTOMER);
        request.addData("id", id);
        return clientSocket.sendRequest(request);
    }

    public Response searchCustomers(String keyword) {
        Request request = new Request(Constants.SEARCH_CUSTOMERS);
        request.addData("keyword", keyword);
        return clientSocket.sendRequest(request);
    }

    // Order Management
    public Response getAllOrders() {
        return clientSocket.sendRequest(new Request(Constants.GET_ALL_ORDERS));
    }

    public Response getOrderById(int id) {
        Request request = new Request(Constants.GET_ORDER_BY_ID);
        request.addData("id", id);
        return clientSocket.sendRequest(request);
    }

    public Response createOrder(Order order) {
        Request request = new Request(Constants.CREATE_ORDER);
        request.addData("order", order);
        return clientSocket.sendRequest(request);
    }

    public Response updateOrderStatus(int id, String status) {
        Request request = new Request(Constants.UPDATE_ORDER_STATUS);
        request.addData("id", id);
        request.addData("status", status);
        return clientSocket.sendRequest(request);
    }

    public Response getOrderItems(int orderId) {
        Request request = new Request(Constants.GET_ORDER_ITEMS);
        request.addData("orderId", orderId);
        return clientSocket.sendRequest(request);
    }

    // Inventory Management
    public Response getAllInventory() {
        return clientSocket.sendRequest(new Request(Constants.GET_ALL_INVENTORY));
    }

    public Response updateInventory(int productId, int quantityChange, String type) {
        Request request = new Request(Constants.UPDATE_INVENTORY);
        request.addData("productId", productId);
        request.addData("quantityChange", quantityChange);
        request.addData("type", type);
        return clientSocket.sendRequest(request);
    }

    public Response getLowStockProducts() {
        return clientSocket.sendRequest(new Request(Constants.GET_LOW_STOCK_PRODUCTS));
    }
}
