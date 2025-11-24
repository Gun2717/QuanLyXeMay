package iuh.fit.se.server.service;

import iuh.fit.se.common.*;
import iuh.fit.se.server.database.DatabaseManager;
import org.apache.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryService {
    private static final Logger logger = Logger.getLogger(InventoryService.class);
    private Connection connection;

    public InventoryService() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public Response getAllInventory() {
        String sql = "SELECT i.*, p.name, p.brand, p.model FROM inventory i " +
                "LEFT JOIN products p ON i.product_id = p.id " +
                "ORDER BY i.id DESC";
        List<Map<String, Object>> inventory = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", rs.getInt("id"));
                item.put("productId", rs.getInt("product_id"));
                item.put("productName", rs.getString("name"));
                item.put("brand", rs.getString("brand"));
                item.put("model", rs.getString("model"));
                item.put("quantityIn", rs.getInt("quantity_in"));
                item.put("quantityOut", rs.getInt("quantity_out"));
                item.put("quantityCurrent", rs.getInt("quantity_current"));
                item.put("lastUpdated", rs.getTimestamp("last_updated"));
                inventory.add(item);
            }
            return Response.success("Lấy tồn kho thành công", inventory);
        } catch (SQLException e) {
            logger.error("Error getting inventory", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response updateInventory(Request request) {
        int productId = (int) request.getData("productId");
        int quantityChange = (int) request.getData("quantityChange");
        String type = (String) request.getData("type"); // "IN" or "OUT"

        try {
            connection.setAutoCommit(false);

            String sql;
            if ("IN".equals(type)) {
                sql = "UPDATE inventory SET quantity_in = quantity_in + ?, quantity_current = quantity_current + ? WHERE product_id = ?";
            } else {
                sql = "UPDATE inventory SET quantity_out = quantity_out + ?, quantity_current = quantity_current - ? WHERE product_id = ?";
            }

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, quantityChange);
                stmt.setInt(2, quantityChange);
                stmt.setInt(3, productId);
                stmt.executeUpdate();
            }

            // Update product quantity
            String productSql = "UPDATE products SET quantity = (SELECT quantity_current FROM inventory WHERE product_id = ?) WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(productSql)) {
                stmt.setInt(1, productId);
                stmt.setInt(2, productId);
                stmt.executeUpdate();
            }

            connection.commit();
            return Response.success("Cập nhật tồn kho thành công", null);

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.error("Error rolling back", ex);
            }
            logger.error("Error updating inventory", e);
            return Response.error("Lỗi: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error("Error setting auto commit", e);
            }
        }
    }

    public Response getLowStockProducts() {
        String sql = "SELECT p.*, i.quantity_current FROM products p " +
                "LEFT JOIN inventory i ON p.id = i.product_id " +
                "WHERE i.quantity_current < 5 " +
                "ORDER BY i.quantity_current ASC";
        List<Map<String, Object>> products = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> product = new HashMap<>();
                product.put("id", rs.getInt("id"));
                product.put("name", rs.getString("name"));
                product.put("brand", rs.getString("brand"));
                product.put("model", rs.getString("model"));
                product.put("quantityCurrent", rs.getInt("quantity_current"));
                products.add(product);
            }
            return Response.success("Lấy sản phẩm sắp hết hàng thành công", products);
        } catch (SQLException e) {
            logger.error("Error getting low stock products", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }
}
