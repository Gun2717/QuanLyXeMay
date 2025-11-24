-- ================================================
-- DATABASE INITIALIZATION SCRIPT
-- Motorcycle Shop Management System
-- ================================================

-- Drop database if exists and create new one
DROP DATABASE IF EXISTS motorcycle_shop;
CREATE DATABASE motorcycle_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE motorcycle_shop;

-- ================================================
-- TABLE: users (Nhân viên)
-- ================================================
CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(100),
                       phone VARCHAR(20),
                       role ENUM('ADMIN', 'STAFF', 'MANAGER') DEFAULT 'STAFF',
                       status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_username (username),
                       INDEX idx_role (role),
                       INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLE: categories (Danh mục xe máy)
-- ================================================
CREATE TABLE categories (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            description TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLE: products (Xe máy)
-- ================================================
CREATE TABLE products (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          category_id INT NOT NULL,
                          name VARCHAR(100) NOT NULL,
                          model VARCHAR(50),
                          brand VARCHAR(100),
                          color VARCHAR(50),
                          price DECIMAL(12, 2) NOT NULL,
                          quantity INT DEFAULT 0,
                          description TEXT,
                          image_path VARCHAR(255),
                          status ENUM('AVAILABLE', 'OUT_OF_STOCK', 'DISCONTINUED') DEFAULT 'AVAILABLE',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
                          INDEX idx_category (category_id),
                          INDEX idx_brand (brand),
                          INDEX idx_status (status),
                          INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLE: customers (Khách hàng)
-- ================================================
CREATE TABLE customers (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           full_name VARCHAR(100) NOT NULL,
                           email VARCHAR(100),
                           phone VARCHAR(20) UNIQUE NOT NULL,
                           address VARCHAR(255),
                           city VARCHAR(100),
                           district VARCHAR(100),
                           loyalty_points INT DEFAULT 0,
                           total_spent DECIMAL(12, 2) DEFAULT 0,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           INDEX idx_phone (phone),
                           INDEX idx_name (full_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLE: orders (Đơn hàng)
-- ================================================
CREATE TABLE orders (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        order_code VARCHAR(50) UNIQUE NOT NULL,
                        customer_id INT,
                        user_id INT NOT NULL,
                        total_amount DECIMAL(12, 2) NOT NULL,
                        discount_amount DECIMAL(12, 2) DEFAULT 0,
                        final_amount DECIMAL(12, 2) NOT NULL,
                        status ENUM('PENDING', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
                        payment_method ENUM('CASH', 'CARD', 'TRANSFER') DEFAULT 'CASH',
                        notes TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
                        FOREIGN KEY (user_id) REFERENCES users(id),
                        INDEX idx_order_code (order_code),
                        INDEX idx_customer (customer_id),
                        INDEX idx_user (user_id),
                        INDEX idx_status (status),
                        INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLE: order_items (Chi tiết đơn hàng)
-- ================================================
CREATE TABLE order_items (
                             id INT PRIMARY KEY AUTO_INCREMENT,
                             order_id INT NOT NULL,
                             product_id INT NOT NULL,
                             quantity INT NOT NULL,
                             unit_price DECIMAL(12, 2) NOT NULL,
                             total_price DECIMAL(12, 2) NOT NULL,
                             FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             FOREIGN KEY (product_id) REFERENCES products(id),
                             INDEX idx_order (order_id),
                             INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLE: inventory (Quản lý tồn kho)
-- ================================================
CREATE TABLE inventory (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           product_id INT NOT NULL UNIQUE,
                           quantity_in INT DEFAULT 0,
                           quantity_out INT DEFAULT 0,
                           quantity_current INT DEFAULT 0,
                           last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                           INDEX idx_product (product_id),
                           INDEX idx_quantity_current (quantity_current)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLE: promotions (Khuyến mãi)
-- ================================================
CREATE TABLE promotions (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL,
                            description TEXT,
                            discount_percent DECIMAL(5, 2),
                            discount_amount DECIMAL(12, 2),
                            start_date DATE NOT NULL,
                            end_date DATE NOT NULL,
                            status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            INDEX idx_status (status),
                            INDEX idx_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLE: promotion_products (Sản phẩm trong khuyến mãi)
-- ================================================
CREATE TABLE promotion_products (
                                    id INT PRIMARY KEY AUTO_INCREMENT,
                                    promotion_id INT NOT NULL,
                                    product_id INT NOT NULL,
                                    FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
                                    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                                    UNIQUE KEY unique_promotion_product (promotion_id, product_id),
                                    INDEX idx_promotion (promotion_id),
                                    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLE: invoices (Hóa đơn)
-- ================================================
CREATE TABLE invoices (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          order_id INT NOT NULL UNIQUE,
                          invoice_code VARCHAR(50) UNIQUE NOT NULL,
                          issue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          due_date DATE,
                          payment_date DATE,
                          paid_amount DECIMAL(12, 2),
                          status ENUM('PENDING', 'PAID', 'OVERDUE') DEFAULT 'PENDING',
                          FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                          INDEX idx_invoice_code (invoice_code),
                          INDEX idx_order (order_id),
                          INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- INSERT SAMPLE DATA
-- ================================================

-- Insert Categories
INSERT INTO categories (name, description) VALUES
                                               ('Xe số', 'Xe máy số tự động, phổ biến cho di chuyển hàng ngày'),
                                               ('Xe côn tay', 'Xe máy côn tay thể thao, phù hợp cho người yêu tốc độ'),
                                               ('Xe ga', 'Xe tay ga tiện lợi, dễ sử dụng'),
                                               ('Xe điện', 'Xe máy điện thân thiện môi trường'),
                                               ('Xe phân khối lớn', 'Xe mô tô phân khối lớn cao cấp');

-- Insert Users (Password: plain text - trong thực tế nên hash)
INSERT INTO users (username, password, full_name, email, phone, role, status) VALUES
                                                                                  ('admin', 'admin123', 'Nguyễn Văn Admin', 'admin@motoshop.vn', '0901234567', 'ADMIN', 'ACTIVE'),
                                                                                  ('manager1', 'manager123', 'Trần Thị Manager', 'manager@motoshop.vn', '0901234568', 'MANAGER', 'ACTIVE'),
                                                                                  ('staff1', 'staff123', 'Lê Văn Nhân Viên', 'staff1@motoshop.vn', '0901234569', 'STAFF', 'ACTIVE'),
                                                                                  ('staff2', 'staff123', 'Phạm Thị Nhân Viên 2', 'staff2@motoshop.vn', '0901234570', 'STAFF', 'ACTIVE'),
                                                                                  ('manager2', 'manager123', 'Hoàng Văn Quản Lý 2', 'manager2@motoshop.vn', '0901234571', 'MANAGER', 'ACTIVE');

-- Insert Products - Honda
INSERT INTO products (category_id, name, model, brand, color, price, quantity, description, status) VALUES
                                                                                                        (1, 'Honda Wave RSX', 'RSX 110cc', 'Honda', 'Đỏ đen', 18500000, 15, 'Xe số tiết kiệm nhiên liệu, phù hợp đi làm', 'AVAILABLE'),
                                                                                                        (1, 'Honda Wave Alpha', 'Alpha 110cc', 'Honda', 'Xanh đen', 17800000, 20, 'Dòng xe số bền bỉ của Honda', 'AVAILABLE'),
                                                                                                        (1, 'Honda Blade', 'Blade 110cc', 'Honda', 'Đỏ', 17500000, 12, 'Xe số thể thao năng động', 'AVAILABLE'),
                                                                                                        (1, 'Honda Future', 'Future 125cc', 'Honda', 'Đen', 30500000, 8, 'Xe số cao cấp, mạnh mẽ', 'AVAILABLE'),
                                                                                                        (2, 'Honda Winner X', 'Winner X 150cc', 'Honda', 'Xanh GP', 47990000, 10, 'Xe côn tay thể thao phong cách', 'AVAILABLE'),
                                                                                                        (2, 'Honda CBR150R', 'CBR150R', 'Honda', 'Đỏ đen', 105000000, 5, 'Xe thể thao cao cấp', 'AVAILABLE'),
                                                                                                        (3, 'Honda Vision', 'Vision 110cc', 'Honda', 'Trắng', 29990000, 18, 'Xe ga tiện lợi cho phái nữ', 'AVAILABLE'),
                                                                                                        (3, 'Honda Air Blade 125', 'Air Blade 125cc', 'Honda', 'Bạc', 41490000, 15, 'Xe ga thể thao cao cấp', 'AVAILABLE'),
                                                                                                        (3, 'Honda SH Mode', 'SH Mode 125cc', 'Honda', 'Đen nhám', 53990000, 10, 'Xe ga sang trọng', 'AVAILABLE'),
                                                                                                        (3, 'Honda SH350i', 'SH350i', 'Honda', 'Đen', 155000000, 3, 'Xe ga phân khối lớn cao cấp', 'AVAILABLE');

-- Insert Products - Yamaha
INSERT INTO products (category_id, name, model, brand, color, price, quantity, description, status) VALUES
                                                                                                        (1, 'Yamaha Sirius', 'Sirius 110cc', 'Yamaha', 'Xanh đen', 20990000, 18, 'Xe số Yamaha bền bỉ', 'AVAILABLE'),
                                                                                                        (2, 'Yamaha Exciter 155', 'Exciter 155cc', 'Yamaha', 'Đỏ đen', 47990000, 12, 'Xe côn tay thể thao mạnh mẽ', 'AVAILABLE'),
                                                                                                        (2, 'Yamaha R15', 'R15 V4', 'Yamaha', 'Xanh GP', 79000000, 6, 'Xe thể thao phong cách đua', 'AVAILABLE'),
                                                                                                        (3, 'Yamaha Janus', 'Janus 125cc', 'Yamaha', 'Xanh pastel', 32990000, 20, 'Xe ga retro độc đáo', 'AVAILABLE'),
                                                                                                        (3, 'Yamaha Grande', 'Grande 125cc', 'Yamaha', 'Nâu vàng', 45500000, 14, 'Xe ga sang trọng cho nữ', 'AVAILABLE'),
                                                                                                        (3, 'Yamaha NVX 155', 'NVX 155cc', 'Yamaha', 'Xám đen', 52990000, 10, 'Xe ga thể thao phân khối lớn', 'AVAILABLE');

-- Insert Products - Other Brands
INSERT INTO products (category_id, name, model, brand, color, price, quantity, description, status) VALUES
                                                                                                        (1, 'Suzuki Raider', 'Raider 150cc', 'Suzuki', 'Đen đỏ', 48900000, 8, 'Xe số thể thao Suzuki', 'AVAILABLE'),
                                                                                                        (2, 'Suzuki GSX-R150', 'GSX-R150', 'Suzuki', 'Xanh trắng', 75000000, 5, 'Xe thể thao Suzuki', 'AVAILABLE'),
                                                                                                        (4, 'VinFast Feliz', 'Feliz S', 'VinFast', 'Trắng', 21000000, 15, 'Xe điện VinFast thông minh', 'AVAILABLE'),
                                                                                                        (4, 'VinFast Evo200', 'Evo200', 'VinFast', 'Đỏ', 41900000, 10, 'Xe điện cao cấp VinFast', 'AVAILABLE'),
                                                                                                        (4, 'VinFast Klara', 'Klara S', 'VinFast', 'Xanh', 33900000, 12, 'Xe điện thời trang', 'AVAILABLE'),
                                                                                                        (5, 'Honda CB500X', 'CB500X', 'Honda', 'Cam đen', 189000000, 2, 'Xe phân khối lớn Adventure', 'AVAILABLE'),
                                                                                                        (5, 'Yamaha MT-15', 'MT-15', 'Yamaha', 'Xám đen', 72000000, 4, 'Naked bike phong cách', 'AVAILABLE');

-- Insert Inventory for all products
INSERT INTO inventory (product_id, quantity_in, quantity_out, quantity_current)
SELECT id, quantity, 0, quantity FROM products;

-- Insert Customers
INSERT INTO customers (full_name, email, phone, address, city, district, loyalty_points, total_spent) VALUES
                                                                                                          ('Nguyễn Văn A', 'nguyenvana@gmail.com', '0912345678', '123 Nguyễn Trãi', 'Hồ Chí Minh', 'Quận 1', 100, 50000000),
                                                                                                          ('Trần Thị B', 'tranthib@gmail.com', '0923456789', '456 Lê Lợi', 'Hồ Chí Minh', 'Quận 3', 50, 30000000),
                                                                                                          ('Lê Văn C', 'levanc@gmail.com', '0934567890', '789 Hai Bà Trưng', 'Hà Nội', 'Hoàn Kiếm', 200, 80000000),
                                                                                                          ('Phạm Thị D', 'phamthid@gmail.com', '0945678901', '321 Trần Hưng Đạo', 'Đà Nẵng', 'Hải Châu', 75, 40000000),
                                                                                                          ('Hoàng Văn E', 'hoangvane@gmail.com', '0956789012', '654 Lý Thường Kiệt', 'Hồ Chí Minh', 'Quận 10', 150, 60000000),
                                                                                                          ('Võ Thị F', 'vothif@gmail.com', '0967890123', '987 Nguyễn Huệ', 'Hồ Chí Minh', 'Quận 1', 30, 20000000),
                                                                                                          ('Đặng Văn G', 'dangvang@gmail.com', '0978901234', '147 Võ Văn Tần', 'Hồ Chí Minh', 'Quận 3', 80, 35000000),
                                                                                                          ('Bùi Thị H', 'buithih@gmail.com', '0989012345', '258 Cách Mạng Tháng 8', 'Hồ Chí Minh', 'Quận 5', 60, 28000000);

-- Insert Sample Orders
INSERT INTO orders (order_code, customer_id, user_id, total_amount, discount_amount, final_amount, status, payment_method, notes) VALUES
                                                                                                                                      ('ORD001', 1, 3, 47990000, 0, 47990000, 'COMPLETED', 'CASH', 'Khách hàng thanh toán tiền mặt'),
                                                                                                                                      ('ORD002', 2, 3, 41490000, 1000000, 40490000, 'COMPLETED', 'TRANSFER', 'Chuyển khoản ngân hàng'),
                                                                                                                                      ('ORD003', 3, 4, 155000000, 5000000, 150000000, 'COMPLETED', 'CARD', 'Thanh toán thẻ tín dụng'),
                                                                                                                                      ('ORD004', 4, 3, 52990000, 0, 52990000, 'PENDING', 'CASH', 'Đang chờ xử lý'),
                                                                                                                                      ('ORD005', 5, 4, 75000000, 2000000, 73000000, 'COMPLETED', 'TRANSFER', 'Đã hoàn thành');

-- Insert Order Items
INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price) VALUES
                                                                                      (1, 5, 1, 47990000, 47990000),
                                                                                      (2, 8, 1, 41490000, 41490000),
                                                                                      (3, 10, 1, 155000000, 155000000),
                                                                                      (4, 16, 1, 52990000, 52990000),
                                                                                      (5, 18, 1, 75000000, 75000000);

-- Insert Promotions
INSERT INTO promotions (name, description, discount_percent, discount_amount, start_date, end_date, status) VALUES
                                                                                                                ('Khuyến mãi Tết 2025', 'Giảm giá đặc biệt dịp Tết Nguyên Đán', 10.00, NULL, '2025-01-15', '2025-02-15', 'ACTIVE'),
                                                                                                                ('Giảm giá Hè', 'Ưu đãi mùa hè cho xe ga', 5.00, NULL, '2025-05-01', '2025-08-31', 'INACTIVE'),
                                                                                                                ('Black Friday', 'Giảm giá sốc cuối năm', NULL, 5000000, '2025-11-20', '2025-11-30', 'INACTIVE');

-- Insert Promotion Products
INSERT INTO promotion_products (promotion_id, product_id) VALUES
                                                              (1, 5), (1, 8), (1, 12), (1, 16),
                                                              (2, 7), (2, 9), (2, 14), (2, 15);

-- ================================================
-- CREATE VIEWS FOR REPORTING
-- ================================================

-- View: Product with Category Info
CREATE OR REPLACE VIEW v_products_detail AS
SELECT
    p.id,
    p.name,
    p.model,
    p.brand,
    p.color,
    p.price,
    p.quantity,
    p.description,
    p.status,
    c.name as category_name,
    i.quantity_current as stock_quantity,
    p.created_at,
    p.updated_at
FROM products p
         LEFT JOIN categories c ON p.category_id = c.id
         LEFT JOIN inventory i ON p.id = i.product_id;

-- View: Order Summary
CREATE OR REPLACE VIEW v_orders_summary AS
SELECT
    o.id,
    o.order_code,
    o.created_at as order_date,
    c.full_name as customer_name,
    c.phone as customer_phone,
    u.full_name as staff_name,
    o.total_amount,
    o.discount_amount,
    o.final_amount,
    o.status,
    o.payment_method,
    COUNT(oi.id) as total_items
FROM orders o
         LEFT JOIN customers c ON o.customer_id = c.id
         LEFT JOIN users u ON o.user_id = u.id
         LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id;

-- View: Low Stock Alert
CREATE OR REPLACE VIEW v_low_stock_alert AS
SELECT
    p.id,
    p.name,
    p.brand,
    p.model,
    p.price,
    i.quantity_current,
    c.name as category_name
FROM products p
         LEFT JOIN inventory i ON p.id = i.product_id
         LEFT JOIN categories c ON p.category_id = c.id
WHERE i.quantity_current < 10 AND p.status = 'AVAILABLE'
ORDER BY i.quantity_current ASC;

-- ================================================
-- STORED PROCEDURES
-- ================================================

DELIMITER //

-- Procedure: Calculate Customer Loyalty Points
CREATE PROCEDURE sp_update_customer_loyalty(
    IN p_customer_id INT,
    IN p_order_amount DECIMAL(12,2)
)
BEGIN
    DECLARE v_points INT;
    SET v_points = FLOOR(p_order_amount / 1000000); -- 1 điểm cho mỗi 1 triệu

UPDATE customers
SET loyalty_points = loyalty_points + v_points,
    total_spent = total_spent + p_order_amount
WHERE id = p_customer_id;
END //

-- Procedure: Get Sales Statistics
CREATE PROCEDURE sp_get_sales_statistics(
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
SELECT
    DATE(created_at) as sale_date,
    COUNT(id) as total_orders,
    SUM(final_amount) as total_revenue,
    AVG(final_amount) as avg_order_value
FROM orders
WHERE DATE(created_at) BETWEEN p_start_date AND p_end_date
  AND status = 'COMPLETED'
GROUP BY DATE(created_at)
ORDER BY sale_date DESC;
END //

DELIMITER ;

-- ================================================
-- INITIAL STATISTICS DATA
-- ================================================

-- Show summary
SELECT
    'Categories' as Table_Name,
    COUNT(*) as Record_Count
FROM categories
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Customers', COUNT(*) FROM customers
UNION ALL
SELECT 'Users', COUNT(*) FROM users
UNION ALL
SELECT 'Orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Items', COUNT(*) FROM order_items
UNION ALL
SELECT 'Inventory', COUNT(*) FROM inventory
UNION ALL
SELECT 'Promotions', COUNT(*) FROM promotions;

-- ================================================
-- COMPLETION MESSAGE
-- ================================================
SELECT '✅ Database initialization completed successfully!' as Status;
SELECT 'Default admin account: username=admin, password=admin123' as Note;
-- Thêm đơn hàng với nhiều ngày khác nhau
USE motorcycle_shop;

-- Xóa đơn hàng cũ (nếu cần)
DELETE FROM order_items WHERE order_id > 0;
DELETE FROM orders WHERE id > 0;

-- Reset auto increment
ALTER TABLE orders AUTO_INCREMENT = 1;
ALTER TABLE order_items AUTO_INCREMENT = 1;

-- Đơn hàng tháng 10/2025
INSERT INTO orders (order_code, customer_id, user_id, total_amount, discount_amount, final_amount, status, payment_method, notes, created_at) VALUES
('ORD20251001', 1, 3, 47990000, 0, 47990000, 'COMPLETED', 'CASH', 'Khách thanh toán tiền mặt', '2025-10-05 09:30:00'),
('ORD20251002', 2, 3, 41490000, 1000000, 40490000, 'COMPLETED', 'TRANSFER', 'Chuyển khoản', '2025-10-08 14:20:00'),
('ORD20251003', 3, 4, 155000000, 5000000, 150000000, 'COMPLETED', 'CARD', 'Thanh toán thẻ', '2025-10-12 11:15:00'),
('ORD20251004', 4, 3, 52990000, 0, 52990000, 'COMPLETED', 'CASH', 'Mua xe ga', '2025-10-15 16:45:00'),
('ORD20251005', 5, 4, 75000000, 2000000, 73000000, 'COMPLETED', 'TRANSFER', 'Khuyến mãi 2 triệu', '2025-10-20 10:30:00'),
('ORD20251006', 6, 3, 29990000, 500000, 29490000, 'COMPLETED', 'CASH', 'Mua Vision', '2025-10-25 13:20:00');

-- Đơn hàng tháng 11/2025
INSERT INTO orders (order_code, customer_id, user_id, total_amount, discount_amount, final_amount, status, payment_method, notes, created_at) VALUES
('ORD20251101', 7, 3, 47990000, 1000000, 46990000, 'COMPLETED', 'TRANSFER', 'Chuyển khoản', '2025-11-02 09:15:00'),
('ORD20251102', 8, 4, 18500000, 0, 18500000, 'COMPLETED', 'CASH', 'Mua Wave RSX', '2025-11-05 14:30:00'),
('ORD20251103', 1, 3, 105000000, 3000000, 102000000, 'COMPLETED', 'CARD', 'Mua CBR150R', '2025-11-08 11:00:00'),
('ORD20251104', 2, 4, 41900000, 0, 41900000, 'COMPLETED', 'TRANSFER', 'Mua xe điện', '2025-11-10 15:20:00'),
('ORD20251105', 3, 3, 53990000, 1500000, 52490000, 'COMPLETED', 'CASH', 'Khuyến mãi', '2025-11-12 10:45:00'),
('ORD20251106', 4, 4, 79000000, 2000000, 77000000, 'COMPLETED', 'CARD', 'Mua R15', '2025-11-15 16:30:00'),
('ORD20251107', 5, 3, 30500000, 0, 30500000, 'COMPLETED', 'CASH', 'Mua Future', '2025-11-18 09:00:00'),
('ORD20251108', 6, 4, 45500000, 1000000, 44500000, 'COMPLETED', 'TRANSFER', 'Mua Grande', '2025-11-20 13:15:00'),
('ORD20251109', 7, 3, 17800000, 0, 17800000, 'COMPLETED', 'CASH', 'Mua Alpha', '2025-11-22 11:30:00'),
('ORD20251110', 8, 4, 32990000, 500000, 32490000, 'PENDING', 'CASH', 'Đang xử lý', '2025-11-23 14:00:00'),
('ORD20251111', 1, 3, 21000000, 0, 21000000, 'PENDING', 'TRANSFER', 'Chờ thanh toán', '2025-11-24 10:00:00');

-- Insert Order Items cho các đơn hàng
-- Tháng 10
INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price) VALUES
(1, 5, 1, 47990000, 47990000),
(2, 8, 1, 41490000, 41490000),
(3, 10, 1, 155000000, 155000000),
(4, 16, 1, 52990000, 52990000),
(5, 18, 1, 75000000, 75000000),
(6, 7, 1, 29990000, 29990000);

-- Tháng 11
INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price) VALUES
(7, 12, 1, 47990000, 47990000),
(8, 1, 1, 18500000, 18500000),
(9, 6, 1, 105000000, 105000000),
(10, 20, 1, 41900000, 41900000),
(11, 9, 1, 53990000, 53990000),
(12, 13, 1, 79000000, 79000000),
(13, 4, 1, 30500000, 30500000),
(14, 15, 1, 45500000, 45500000),
(15, 2, 1, 17800000, 17800000),
(16, 14, 1, 32990000, 32990000),
(17, 19, 1, 21000000, 21000000);

-- Cập nhật số lượng sản phẩm sau khi bán
UPDATE products SET quantity = quantity - 1 WHERE id IN (1,2,4,5,6,7,8,9,10,12,13,14,15,16,18,19,20);

-- Cập nhật tồn kho
UPDATE inventory i
JOIN products p ON i.product_id = p.id
SET i.quantity_out = i.quantity_out + 1,
    i.quantity_current = i.quantity_current - 1
WHERE p.id IN (1,2,4,5,6,7,8,9,10,12,13,14,15,16,18,19,20);

-- Kiểm tra kết quả
SELECT
    DATE(created_at) as ngay,
    COUNT(*) as so_don_hang,
    SUM(final_amount) as tong_doanh_thu
FROM orders
GROUP BY DATE(created_at)
ORDER BY ngay DESC;

SELECT
    o.order_code,
    DATE_FORMAT(o.created_at, '%d/%m/%Y %H:%i') as ngay_tao,
    COALESCE(c.full_name, 'Khách lẻ') as khach_hang,
    u.full_name as nhan_vien,
    FORMAT(o.final_amount, 0) as thanh_tien,
    o.status as trang_thai
FROM orders o
LEFT JOIN customers c ON o.customer_id = c.id
LEFT JOIN users u ON o.user_id = u.id
ORDER BY o.created_at DESC;