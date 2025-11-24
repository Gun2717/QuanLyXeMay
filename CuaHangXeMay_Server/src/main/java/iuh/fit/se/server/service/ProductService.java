package iuh.fit.se.server.service;

import iuh.fit.se.common.*;
import iuh.fit.se.server.database.DatabaseManager;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private static final Logger logger = Logger.getLogger(ProductService.class);
    private Connection connection;

    public ProductService() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public Response getAllProducts() {
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "ORDER BY p.id DESC";
        List<Product> products = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(extractProduct(rs));
            }

            return Response.success("Lấy danh sách sản phẩm thành công", products);
        } catch (SQLException e) {
            logger.error("Error getting all products", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response getProductById(Request request) {
        int id = (int) request.getData("id");
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Response.success("Lấy thông tin sản phẩm thành công", extractProduct(rs));
            } else {
                return Response.notFound("Không tìm thấy sản phẩm");
            }
        } catch (SQLException e) {
            logger.error("Error getting product by id", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response createProduct(Request request) {
        Product product = (Product) request.getData("product");

        String sql = "INSERT INTO products (category_id, name, model, brand, color, " +
                "price, quantity, description, image_path, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, product.getCategoryId());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getModel());
            stmt.setString(4, product.getBrand());
            stmt.setString(5, product.getColor());
            stmt.setBigDecimal(6, product.getPrice());
            stmt.setInt(7, product.getQuantity());
            stmt.setString(8, product.getDescription());
            stmt.setString(9, product.getImagePath());
            stmt.setString(10, product.getStatus());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    product.setId(rs.getInt(1));

                    // Create inventory record
                    createInventoryForProduct(product.getId(), product.getQuantity());
                }
                logger.info("Product created: " + product.getName());
                return Response.success("Tạo sản phẩm thành công", product);
            } else {
                return Response.error("Không thể tạo sản phẩm");
            }
        } catch (SQLException e) {
            logger.error("Error creating product", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response updateProduct(Request request) {
        Product product = (Product) request.getData("product");

        String sql = "UPDATE products SET category_id = ?, name = ?, model = ?, " +
                "brand = ?, color = ?, price = ?, quantity = ?, " +
                "description = ?, image_path = ?, status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, product.getCategoryId());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getModel());
            stmt.setString(4, product.getBrand());
            stmt.setString(5, product.getColor());
            stmt.setBigDecimal(6, product.getPrice());
            stmt.setInt(7, product.getQuantity());
            stmt.setString(8, product.getDescription());
            stmt.setString(9, product.getImagePath());
            stmt.setString(10, product.getStatus());
            stmt.setInt(11, product.getId());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                logger.info("Product updated: " + product.getId());
                return Response.success("Cập nhật sản phẩm thành công", product);
            } else {
                return Response.error("Không thể cập nhật sản phẩm");
            }
        } catch (SQLException e) {
            logger.error("Error updating product", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response deleteProduct(Request request) {
        int id = (int) request.getData("id");
        String sql = "DELETE FROM products WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                logger.info("Product deleted: " + id);
                return Response.success("Xóa sản phẩm thành công", null);
            } else {
                return Response.error("Không thể xóa sản phẩm");
            }
        } catch (SQLException e) {
            logger.error("Error deleting product", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response searchProducts(Request request) {
        String keyword = (String) request.getData("keyword");
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.name LIKE ? OR p.brand LIKE ? OR p.model LIKE ?";

        List<Product> products = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(extractProduct(rs));
            }

            return Response.success("Tìm kiếm thành công", products);
        } catch (SQLException e) {
            logger.error("Error searching products", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response getProductsByCategory(Request request) {
        int categoryId = (int) request.getData("categoryId");
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.category_id = ?";

        List<Product> products = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(extractProduct(rs));
            }

            return Response.success("Lấy sản phẩm theo danh mục thành công", products);
        } catch (SQLException e) {
            logger.error("Error getting products by category", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response getAllCategories() {
        String sql = "SELECT * FROM categories ORDER BY name";
        List<Category> categories = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                category.setCreatedAt(rs.getTimestamp("created_at"));
                categories.add(category);
            }

            return Response.success("Lấy danh mục thành công", categories);
        } catch (SQLException e) {
            logger.error("Error getting categories", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    public Response createCategory(Request request) {
        Category category = (Category) request.getData("category");
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    category.setId(rs.getInt(1));
                }
                return Response.success("Tạo danh mục thành công", category);
            } else {
                return Response.error("Không thể tạo danh mục");
            }
        } catch (SQLException e) {
            logger.error("Error creating category", e);
            return Response.error("Lỗi: " + e.getMessage());
        }
    }

    private void createInventoryForProduct(int productId, int initialQuantity) {
        String sql = "INSERT INTO inventory (product_id, quantity_in, quantity_current) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, initialQuantity);
            stmt.setInt(3, initialQuantity);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error creating inventory", e);
        }
    }

    private Product extractProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setCategoryName(rs.getString("category_name"));
        product.setName(rs.getString("name"));
        product.setModel(rs.getString("model"));
        product.setBrand(rs.getString("brand"));
        product.setColor(rs.getString("color"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setQuantity(rs.getInt("quantity"));
        product.setDescription(rs.getString("description"));
        product.setImagePath(rs.getString("image_path"));
        product.setStatus(rs.getString("status"));
        product.setCreatedAt(rs.getTimestamp("created_at"));
        product.setUpdatedAt(rs.getTimestamp("updated_at"));
        return product;
    }
}
