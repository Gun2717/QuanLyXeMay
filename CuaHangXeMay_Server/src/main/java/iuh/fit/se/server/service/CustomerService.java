package iuh.fit.se.server.service;

import iuh.fit.se.common.*;
import iuh.fit.se.server.database.DatabaseManager;
import org.apache.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {
    private static final Logger logger = Logger.getLogger(CustomerService.class);
    private Connection connection;

    public CustomerService() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public Response getAllCustomers() {
        String sql = "SELECT * FROM customers ORDER BY id DESC";
        List<Customer> customers = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(extractCustomer(rs));
            }
            return Response.success("Lấy danh sách khách hàng thành công", customers);
        } catch (SQLException e) {
            logger.error("Error getting customers", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response getCustomerById(Request request) {
        int id = (int) request.getData("id");
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Response.success("Lấy thông tin khách hàng thành công", extractCustomer(rs));
            } else {
                return Response.notFound("Không tìm thấy khách hàng");
            }
        } catch (SQLException e) {
            logger.error("Error getting customer", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response createCustomer(Request request) {
        Customer customer = (Customer) request.getData("customer");
        String sql = "INSERT INTO customers (full_name, email, phone, address, city, district) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getAddress());
            stmt.setString(5, customer.getCity());
            stmt.setString(6, customer.getDistrict());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) customer.setId(rs.getInt(1));
                return Response.success("Tạo khách hàng thành công", customer);
            }
            return Response.error("Không thể tạo khách hàng");
        } catch (SQLException e) {
            logger.error("Error creating customer", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response updateCustomer(Request request) {
        Customer customer = (Customer) request.getData("customer");
        String sql = "UPDATE customers SET full_name=?, email=?, phone=?, address=?, city=?, district=? WHERE id=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getAddress());
            stmt.setString(5, customer.getCity());
            stmt.setString(6, customer.getDistrict());
            stmt.setInt(7, customer.getId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                return Response.success("Cập nhật khách hàng thành công", customer);
            }
            return Response.error("Không thể cập nhật khách hàng");
        } catch (SQLException e) {
            logger.error("Error updating customer", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response deleteCustomer(Request request) {
        int id = (int) request.getData("id");
        String sql = "DELETE FROM customers WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                return Response.success("Xóa khách hàng thành công", null);
            }
            return Response.error("Không thể xóa khách hàng");
        } catch (SQLException e) {
            logger.error("Error deleting customer", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response searchCustomers(Request request) {
        String keyword = (String) request.getData("keyword");
        String sql = "SELECT * FROM customers WHERE full_name LIKE ? OR phone LIKE ?";
        List<Customer> customers = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String search = "%" + keyword + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                customers.add(extractCustomer(rs));
            }
            return Response.success("Tìm kiếm thành công", customers);
        } catch (SQLException e) {
            logger.error("Error searching customers", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    private Customer extractCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setFullName(rs.getString("full_name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setCity(rs.getString("city"));
        c.setDistrict(rs.getString("district"));
        c.setLoyaltyPoints(rs.getInt("loyalty_points"));
        c.setTotalSpent(rs.getBigDecimal("total_spent"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));
        return c;
    }
}
