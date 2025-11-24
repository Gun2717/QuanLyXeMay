package iuh.fit.se.server.service;

import iuh.fit.se.common.*;
import iuh.fit.se.server.database.DatabaseManager;
import org.apache.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private static final Logger logger = Logger.getLogger(OrderService.class);
    private Connection connection;

    public OrderService() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public Response getAllOrders() {
        String sql = "SELECT o.*, c.full_name as customer_name, u.full_name as user_name " +
                "FROM orders o " +
                "LEFT JOIN customers c ON o.customer_id = c.id " +
                "LEFT JOIN users u ON o.user_id = u.id " +
                "ORDER BY o.id DESC";
        List<Order> orders = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(extractOrder(rs));
            }
            return Response.success("Lấy danh sách đơn hàng thành công", orders);
        } catch (SQLException e) {
            logger.error("Error getting orders", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response getOrderById(Request request) {
        int id = (int) request.getData("id");
        String sql = "SELECT o.*, c.full_name as customer_name, u.full_name as user_name " +
                "FROM orders o " +
                "LEFT JOIN customers c ON o.customer_id = c.id " +
                "LEFT JOIN users u ON o.user_id = u.id " +
                "WHERE o.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Order order = extractOrder(rs);
                // Get order items
                order.setOrderItems(getOrderItemsByOrderId(id));
                return Response.success("Lấy thông tin đơn hàng thành công", order);
            }
            return Response.notFound("Không tìm thấy đơn hàng");
        } catch (SQLException e) {
            logger.error("Error getting order", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response createOrder(Request request) {
        Order order = (Order) request.getData("order");

        try {
            connection.setAutoCommit(false);

            // Insert order
            String orderSql = "INSERT INTO orders (order_code, customer_id, user_id, total_amount, " +
                    "discount_amount, final_amount, status, payment_method, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, generateOrderCode());
                stmt.setInt(2, order.getCustomerId());
                stmt.setInt(3, order.getUserId());
                stmt.setBigDecimal(4, order.getTotalAmount());
                stmt.setBigDecimal(5, order.getDiscountAmount());
                stmt.setBigDecimal(6, order.getFinalAmount());
                stmt.setString(7, order.getStatus());
                stmt.setString(8, order.getPaymentMethod());
                stmt.setString(9, order.getNotes());

                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    order.setId(rs.getInt(1));
                }
            }

            // Insert order items
            String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(itemSql)) {
                for (OrderItem item : order.getOrderItems()) {
                    stmt.setInt(1, order.getId());
                    stmt.setInt(2, item.getProductId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setBigDecimal(4, item.getUnitPrice());
                    stmt.setBigDecimal(5, item.getTotalPrice());
                    stmt.executeUpdate();

                    // Update product quantity
                    updateProductQuantity(item.getProductId(), -item.getQuantity());
                }
            }

            connection.commit();
            return Response.success("Tạo đơn hàng thành công", order);

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.error("Error rolling back", ex);
            }
            logger.error("Error creating order", e);
            return Response.error("Lỗi: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error("Error setting auto commit", e);
            }
        }
    }

    public Response updateOrderStatus(Request request) {
        int id = (int) request.getData("id");
        String status = (String) request.getData("status");
        String sql = "UPDATE orders SET status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                return Response.success("Cập nhật trạng thái thành công", null);
            }
            return Response.error("Không thể cập nhật trạng thái");
        } catch (SQLException e) {
            logger.error("Error updating order status", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response getOrderItems(Request request) {
        int orderId = (int) request.getData("orderId");
        List<OrderItem> items = getOrderItemsByOrderId(orderId);
        return Response.success("Lấy chi tiết đơn hàng thành công", items);
    }

    private List<OrderItem> getOrderItemsByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, p.name as product_name FROM order_items oi " +
                "LEFT JOIN products p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
                item.setTotalPrice(rs.getBigDecimal("total_price"));
                items.add(item);
            }
        } catch (SQLException e) {
            logger.error("Error getting order items", e);
        }
        return items;
    }

    private void updateProductQuantity(int productId, int quantityChange) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, quantityChange);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis();
    }

    private Order extractOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setOrderCode(rs.getString("order_code"));
        o.setCustomerId(rs.getInt("customer_id"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setUserId(rs.getInt("user_id"));
        o.setUserName(rs.getString("user_name"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        o.setFinalAmount(rs.getBigDecimal("final_amount"));
        o.setStatus(rs.getString("status"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setNotes(rs.getString("notes"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        o.setUpdatedAt(rs.getTimestamp("updated_at"));
        return o;
    }
}
