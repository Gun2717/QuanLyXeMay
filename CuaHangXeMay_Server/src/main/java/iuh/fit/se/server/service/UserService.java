package iuh.fit.se.server.service;

import iuh.fit.se.common.*;
import iuh.fit.se.server.database.DatabaseManager;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class);
    private Connection connection;

    public UserService() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public Response login(Request request) {
        String username = (String) request.getData("username");
        String password = (String) request.getData("password");

        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'ACTIVE'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = extractUser(rs);
                user.setPassword(null); // Don't send password to client
                logger.info("User logged in: " + username);
                return Response.success("Đăng nhập thành công", user);
            } else {
                return Response.error("Tên đăng nhập hoặc mật khẩu không đúng");
            }
        } catch (SQLException e) {
            logger.error("Login error", e);
            return Response.error("Lỗi đăng nhập: " + e.getMessage());
        }
    }

    public Response getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY id DESC";
        List<User> users = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = extractUser(rs);
                user.setPassword(null);
                users.add(user);
            }

            return Response.success("Lấy danh sách người dùng thành công", users);
        } catch (SQLException e) {
            logger.error("Error getting all users", e);
            return Response.error("Lỗi lấy danh sách: " + e.getMessage());
        }
    }

    public Response getUserById(Request request) {
        int id = (int) request.getData("id");
        String sql = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = extractUser(rs);
                user.setPassword(null);
                return Response.success("Lấy thông tin người dùng thành công", user);
            } else {
                return Response.notFound("Không tìm thấy người dùng");
            }
        } catch (SQLException e) {
            logger.error("Error getting user by id", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response createUser(Request request) {
        User user = (User) request.getData("user");

        String sql = "INSERT INTO users (username, password, full_name, email, phone, role, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getRole());
            stmt.setString(7, user.getStatus());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
                logger.info("User created: " + user.getUsername());
                return Response.success("Tạo người dùng thành công", user);
            } else {
                return Response.error("Không thể tạo người dùng");
            }
        } catch (SQLException e) {
            logger.error("Error creating user", e);
            if (e.getMessage().contains("Duplicate entry")) {
                return Response.error("Tên đăng nhập đã tồn tại");
            }
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response updateUser(Request request) {
        User user = (User) request.getData("user");

        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, " +
                "role = ?, status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getStatus());
            stmt.setInt(6, user.getId());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                logger.info("User updated: " + user.getId());
                return Response.success("Cập nhật người dùng thành công", user);
            } else {
                return Response.error("Không thể cập nhật người dùng");
            }
        } catch (SQLException e) {
            logger.error("Error updating user", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response deleteUser(Request request) {
        int id = (int) request.getData("id");
        String sql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                logger.info("User deleted: " + id);
                return Response.success("Xóa người dùng thành công", null);
            } else {
                return Response.error("Không thể xóa người dùng");
            }
        } catch (SQLException e) {
            logger.error("Error deleting user", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}
