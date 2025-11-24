package iuh.fit.se.common;

public class Constants {
    // Server Configuration
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 9999;

    // Request Types
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";

    // User Management
    public static final String GET_ALL_USERS = "GET_ALL_USERS";
    public static final String GET_USER_BY_ID = "GET_USER_BY_ID";
    public static final String CREATE_USER = "CREATE_USER";
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String DELETE_USER = "DELETE_USER";
    public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";

    // Product Management
    public static final String GET_ALL_PRODUCTS = "GET_ALL_PRODUCTS";
    public static final String GET_PRODUCT_BY_ID = "GET_PRODUCT_BY_ID";
    public static final String CREATE_PRODUCT = "CREATE_PRODUCT";
    public static final String UPDATE_PRODUCT = "UPDATE_PRODUCT";
    public static final String DELETE_PRODUCT = "DELETE_PRODUCT";
    public static final String SEARCH_PRODUCTS = "SEARCH_PRODUCTS";
    public static final String GET_PRODUCTS_BY_CATEGORY = "GET_PRODUCTS_BY_CATEGORY";

    // Category Management
    public static final String GET_ALL_CATEGORIES = "GET_ALL_CATEGORIES";
    public static final String CREATE_CATEGORY = "CREATE_CATEGORY";

    // Customer Management
    public static final String GET_ALL_CUSTOMERS = "GET_ALL_CUSTOMERS";
    public static final String GET_CUSTOMER_BY_ID = "GET_CUSTOMER_BY_ID";
    public static final String CREATE_CUSTOMER = "CREATE_CUSTOMER";
    public static final String UPDATE_CUSTOMER = "UPDATE_CUSTOMER";
    public static final String DELETE_CUSTOMER = "DELETE_CUSTOMER";
    public static final String SEARCH_CUSTOMERS = "SEARCH_CUSTOMERS";

    // Order Management
    public static final String GET_ALL_ORDERS = "GET_ALL_ORDERS";
    public static final String GET_ORDER_BY_ID = "GET_ORDER_BY_ID";
    public static final String CREATE_ORDER = "CREATE_ORDER";
    public static final String UPDATE_ORDER_STATUS = "UPDATE_ORDER_STATUS";
    public static final String DELETE_ORDER = "DELETE_ORDER";
    public static final String GET_ORDERS_BY_CUSTOMER = "GET_ORDERS_BY_CUSTOMER";
    public static final String GET_ORDER_ITEMS = "GET_ORDER_ITEMS";

    // Inventory Management
    public static final String GET_ALL_INVENTORY = "GET_ALL_INVENTORY";
    public static final String UPDATE_INVENTORY = "UPDATE_INVENTORY";
    public static final String GET_LOW_STOCK_PRODUCTS = "GET_LOW_STOCK_PRODUCTS";

    // Promotion Management
    public static final String GET_ALL_PROMOTIONS = "GET_ALL_PROMOTIONS";
    public static final String CREATE_PROMOTION = "CREATE_PROMOTION";
    public static final String UPDATE_PROMOTION = "UPDATE_PROMOTION";
    public static final String DELETE_PROMOTION = "DELETE_PROMOTION";

    // Statistics
    public static final String GET_REVENUE_STATISTICS = "GET_REVENUE_STATISTICS";
    public static final String GET_TOP_SELLING_PRODUCTS = "GET_TOP_SELLING_PRODUCTS";
    public static final String GET_CUSTOMER_STATISTICS = "GET_CUSTOMER_STATISTICS";

    // Response Status
    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String NOT_FOUND = "NOT_FOUND";

    // User Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_STAFF = "STAFF";

    // Product Status
    public static final String AVAILABLE = "AVAILABLE";
    public static final String OUT_OF_STOCK = "OUT_OF_STOCK";
    public static final String DISCONTINUED = "DISCONTINUED";

    // Order Status
    public static final String ORDER_PENDING = "PENDING";
    public static final String ORDER_COMPLETED = "COMPLETED";
    public static final String ORDER_CANCELLED = "CANCELLED";

    // Payment Methods
    public static final String PAYMENT_CASH = "CASH";
    public static final String PAYMENT_CARD = "CARD";
    public static final String PAYMENT_TRANSFER = "TRANSFER";
}
