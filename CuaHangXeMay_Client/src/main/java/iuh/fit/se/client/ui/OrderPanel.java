package iuh.fit.se.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import iuh.fit.se.client.service.ApiService;
import iuh.fit.se.common.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderPanel extends JPanel {
    private User currentUser;
    private ApiService apiService;
    private Gson gson;

    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JButton btnRefresh, btnViewDetails, btnUpdateStatus, btnCreateOrder;

    public OrderPanel(User user) {
        this.currentUser = user;
        this.apiService = new ApiService();
        this.gson = new Gson();
        initComponents();
        loadOrders();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("QUẢN LÝ ĐƠN HÀNG");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(52, 73, 94));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setBackground(Color.WHITE);

        btnCreateOrder = createButton("➕ Tạo đơn hàng", new Color(46, 204, 113));
        btnCreateOrder.addActionListener(e -> showCreateOrderDialog());

        btnViewDetails = createButton("👁️ Xem chi tiết", new Color(52, 152, 219));
        btnViewDetails.addActionListener(e -> viewOrderDetails());

        btnUpdateStatus = createButton("📝 Cập nhật trạng thái", new Color(241, 196, 15));
        btnUpdateStatus.addActionListener(e -> updateOrderStatus());

        btnRefresh = createButton("🔄 Làm mới", new Color(149, 165, 166));
        btnRefresh.addActionListener(e -> loadOrders());

        toolbarPanel.add(btnCreateOrder);
        toolbarPanel.add(btnViewDetails);
        toolbarPanel.add(btnUpdateStatus);
        toolbarPanel.add(btnRefresh);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Mã đơn", "Khách hàng", "Nhân viên", "Tổng tiền", "Giảm giá", "Thành tiền", "Trạng thái", "Thanh toán", "Ngày tạo"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderTable = new JTable(tableModel);
        orderTable.setFont(new Font("Arial", Font.PLAIN, 11));
        orderTable.setRowHeight(30);
        orderTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        orderTable.getTableHeader().setBackground(new Color(52, 73, 94));
        orderTable.getTableHeader().setForeground(Color.WHITE);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 35));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public void refresh() {
        loadOrders();
    }

    private void loadOrders() {
        tableModel.setRowCount(0);

        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws Exception {
                return apiService.getAllOrders();
            }

            @Override
            protected void done() {
                try {
                    Response response = get();
                    if (response.isSuccess()) {
                        String json = gson.toJson(response.getData());
                        List<Order> orders = gson.fromJson(json, new TypeToken<List<Order>>(){}.getType());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                        for (Order o : orders) {
                            tableModel.addRow(new Object[]{
                                    o.getId(),
                                    o.getOrderCode(),
                                    o.getCustomerName() != null ? o.getCustomerName() : "Khách lẻ",
                                    o.getUserName(),
                                    String.format("%,.0f đ", o.getTotalAmount()),
                                    String.format("%,.0f đ", o.getDiscountAmount()),
                                    String.format("%,.0f đ", o.getFinalAmount()),
                                    getStatusBadge(o.getStatus()),
                                    o.getPaymentMethod(),
                                    sdf.format(o.getCreatedAt())
                            });
                        }
                    } else {
                        JOptionPane.showMessageDialog(OrderPanel.this,
                                response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(OrderPanel.this,
                            "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private String getStatusBadge(String status) {
        switch (status) {
            case "PENDING": return "⏳ Chờ xử lý";
            case "COMPLETED": return "✅ Hoàn thành";
            case "CANCELLED": return "❌ Đã hủy";
            default: return status;
        }
    }

    private void showCreateOrderDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Tạo đơn hàng mới", true);
        dialog.setSize(1000, 750);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Customer Selection Panel
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createTitledBorder("Thông tin khách hàng"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Get customers
        Response custResponse = apiService.getAllCustomers();
        List<Customer> customers = new ArrayList<>();
        if (custResponse.isSuccess()) {
            String json = gson.toJson(custResponse.getData());
            customers = gson.fromJson(json, new TypeToken<List<Customer>>(){}.getType());
        }

        JComboBox<String> cboCustomer = new JComboBox<>();
        cboCustomer.addItem("Khách lẻ");
        for (Customer c : customers) {
            cboCustomer.addItem(c.getId() + " - " + c.getFullName() + " - " + c.getPhone());
        }

        // Fields for walk-in customer
        JLabel lblWalkInName = new JLabel("Tên khách hàng:");
        JTextField txtWalkInName = new JTextField(20);
        JLabel lblWalkInPhone = new JLabel("Số điện thoại:");
        JTextField txtWalkInPhone = new JTextField(20);

        // Initially show walk-in fields
        lblWalkInName.setVisible(true);
        txtWalkInName.setVisible(true);
        lblWalkInPhone.setVisible(true);
        txtWalkInPhone.setVisible(true);

        // Add listener to toggle walk-in fields
        cboCustomer.addActionListener(e -> {
            boolean isWalkIn = cboCustomer.getSelectedIndex() == 0;
            lblWalkInName.setVisible(isWalkIn);
            txtWalkInName.setVisible(isWalkIn);
            lblWalkInPhone.setVisible(isWalkIn);
            txtWalkInPhone.setVisible(isWalkIn);
            topPanel.revalidate();
            topPanel.repaint();
        });

        String[] paymentMethods = {"CASH", "CARD", "TRANSFER"};
        JComboBox<String> cboPayment = new JComboBox<>(paymentMethods);

        JTextField txtDiscount = new JTextField("0", 10);
        JTextArea txtNotes = new JTextArea(2, 30);
        txtNotes.setLineWrap(true);

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Loại khách hàng:"), gbc);
        gbc.gridx = 1;
        topPanel.add(cboCustomer, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        topPanel.add(lblWalkInName, gbc);
        gbc.gridx = 1;
        topPanel.add(txtWalkInName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        topPanel.add(lblWalkInPhone, gbc);
        gbc.gridx = 1;
        topPanel.add(txtWalkInPhone, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        topPanel.add(new JLabel("Thanh toán:"), gbc);
        gbc.gridx = 1;
        topPanel.add(cboPayment, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        topPanel.add(new JLabel("Giảm giá (đ):"), gbc);
        gbc.gridx = 1;
        topPanel.add(txtDiscount, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        topPanel.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 1;
        topPanel.add(new JScrollPane(txtNotes), gbc);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Products Selection Panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Chọn sản phẩm"));

        // Product table
        String[] productCols = {"ID", "Tên sản phẩm", "Hãng", "Giá", "Tồn kho", "Số lượng"};
        DefaultTableModel productModel = new DefaultTableModel(productCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Integer.class;
                return String.class;
            }
        };

        JTable productTable = new JTable(productModel);
        productTable.setRowHeight(25);

        // Load products
        Response prodResponse = apiService.getAllProducts();
        if (prodResponse.isSuccess()) {
            String json = gson.toJson(prodResponse.getData());
            List<Product> products = gson.fromJson(json, new TypeToken<List<Product>>(){}.getType());
            for (Product p : products) {
                if ("AVAILABLE".equals(p.getStatus()) && p.getQuantity() > 0) {
                    productModel.addRow(new Object[]{
                            p.getId(),
                            p.getName(),
                            p.getBrand(),
                            String.format("%,.0f đ", p.getPrice()),
                            p.getQuantity(),
                            0
                    });
                }
            }
        }

        JScrollPane productScroll = new JScrollPane(productTable);
        centerPanel.add(productScroll, BorderLayout.CENTER);

        // Total Panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel lblTotal = new JLabel("Tổng tiền: 0 đ");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(new Color(231, 76, 60));
        totalPanel.add(lblTotal);

        // Update total when quantity changes
        productModel.addTableModelListener(e -> {
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < productModel.getRowCount(); i++) {
                int qty = (Integer) productModel.getValueAt(i, 5);
                if (qty > 0) {
                    String priceStr = productModel.getValueAt(i, 3).toString().replace(" đ", "").replace(",", "");
                    BigDecimal price = new BigDecimal(priceStr);
                    total = total.add(price.multiply(new BigDecimal(qty)));
                }
            }
            lblTotal.setText(String.format("Tổng tiền: %,.0f đ", total));
        });

        centerPanel.add(totalPanel, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton btnSave = createButton("💾 Tạo đơn hàng", new Color(46, 204, 113));
        JButton btnCancel = createButton("❌ Hủy", new Color(231, 76, 60));

        List<Customer> finalCustomers = customers;
        btnSave.addActionListener(e -> {
            try {
                // Validate walk-in customer info if selected
                if (cboCustomer.getSelectedIndex() == 0) {
                    String walkInName = txtWalkInName.getText().trim();
                    String walkInPhone = txtWalkInPhone.getText().trim();

                    if (walkInName.isEmpty() || walkInPhone.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog,
                                "Vui lòng nhập đầy đủ tên và số điện thoại khách lẻ!",
                                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                // Validate products
                List<OrderItem> items = new ArrayList<>();
                BigDecimal totalAmount = BigDecimal.ZERO;

                for (int i = 0; i < productModel.getRowCount(); i++) {
                    int qty = (Integer) productModel.getValueAt(i, 5);
                    if (qty > 0) {
                        int productId = (int) productModel.getValueAt(i, 0);
                        int stock = (int) productModel.getValueAt(i, 4);

                        if (qty > stock) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Sản phẩm " + productModel.getValueAt(i, 1) + " không đủ số lượng trong kho!",
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        String priceStr = productModel.getValueAt(i, 3).toString().replace(" đ", "").replace(",", "");
                        BigDecimal price = new BigDecimal(priceStr);
                        BigDecimal itemTotal = price.multiply(new BigDecimal(qty));

                        OrderItem item = new OrderItem();
                        item.setProductId(productId);
                        item.setQuantity(qty);
                        item.setUnitPrice(price);
                        item.setTotalPrice(itemTotal);
                        items.add(item);

                        totalAmount = totalAmount.add(itemTotal);
                    }
                }

                if (items.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng chọn ít nhất một sản phẩm!",
                            "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Create order
                Order order = new Order();

                // Set customer
                String customerStr = (String) cboCustomer.getSelectedItem();
                if ("Khách lẻ".equals(customerStr)) {
                    order.setCustomerName(txtWalkInName.getText().trim());
                } else {
                    int customerId = Integer.parseInt(customerStr.split(" - ")[0]);
                    order.setCustomerId(customerId);
                }

                order.setUserId(currentUser.getId());
                order.setTotalAmount(totalAmount);

                BigDecimal discount = new BigDecimal(txtDiscount.getText().trim());
                order.setDiscountAmount(discount);
                order.setFinalAmount(totalAmount.subtract(discount));

                order.setStatus("PENDING");
                order.setPaymentMethod((String) cboPayment.getSelectedItem());
                order.setNotes(txtNotes.getText().trim());
                order.setOrderItems(items);

                // Save order
                Response response = apiService.createOrder(order);
                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Tạo đơn hàng thành công!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadOrders();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, response.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá trị giảm giá không hợp lệ!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void viewOrderDetails() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn hàng!");
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);
        Response response = apiService.getOrderById(orderId);

        if (response.isSuccess()) {
            String json = gson.toJson(response.getData());
            Order order = gson.fromJson(json, Order.class);

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết đơn hàng", true);
            dialog.setSize(700, 500);
            dialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // Order info
            StringBuilder info = new StringBuilder();
            info.append("Mã đơn: ").append(order.getOrderCode()).append("\n");
            info.append("Khách hàng: ").append(order.getCustomerName() != null ? order.getCustomerName() : "Khách lẻ").append("\n");
            info.append("Nhân viên: ").append(order.getUserName()).append("\n");
            info.append("Trạng thái: ").append(getStatusBadge(order.getStatus())).append("\n");
            info.append("Thanh toán: ").append(order.getPaymentMethod()).append("\n");
            info.append("Ghi chú: ").append(order.getNotes() != null ? order.getNotes() : "Không có").append("\n\n");
            info.append("─────────────────────────────────────\n\n");

            JTextArea txtInfo = new JTextArea(info.toString());
            txtInfo.setFont(new Font("Arial", Font.PLAIN, 13));
            txtInfo.setEditable(false);
            panel.add(new JScrollPane(txtInfo), BorderLayout.NORTH);

            // Order items table
            String[] cols = {"Sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"};
            DefaultTableModel itemModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable itemTable = new JTable(itemModel);
            itemTable.setRowHeight(25);

            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    itemModel.addRow(new Object[]{
                            item.getProductName(),
                            item.getQuantity(),
                            String.format("%,.0f đ", item.getUnitPrice()),
                            String.format("%,.0f đ", item.getTotalPrice())
                    });
                }
            }

            panel.add(new JScrollPane(itemTable), BorderLayout.CENTER);

            // Total panel
            JPanel totalPanel = new JPanel(new GridLayout(3, 2, 10, 5));
            totalPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

            totalPanel.add(new JLabel("Tổng tiền:"));
            totalPanel.add(new JLabel(String.format("%,.0f đ", order.getTotalAmount())));

            totalPanel.add(new JLabel("Giảm giá:"));
            totalPanel.add(new JLabel(String.format("%,.0f đ", order.getDiscountAmount())));

            JLabel lblFinal = new JLabel("Thành tiền:");
            lblFinal.setFont(new Font("Arial", Font.BOLD, 14));
            totalPanel.add(lblFinal);

            JLabel lblFinalAmount = new JLabel(String.format("%,.0f đ", order.getFinalAmount()));
            lblFinalAmount.setFont(new Font("Arial", Font.BOLD, 14));
            lblFinalAmount.setForeground(new Color(231, 76, 60));
            totalPanel.add(lblFinalAmount);

            panel.add(totalPanel, BorderLayout.SOUTH);

            dialog.add(panel);
            dialog.setVisible(true);
        }
    }

    private void updateOrderStatus() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn hàng!");
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);
        String[] statuses = {"PENDING", "COMPLETED", "CANCELLED"};
        String[] statusLabels = {"⏳ Chờ xử lý", "✅ Hoàn thành", "❌ Đã hủy"};

        String selected = (String) JOptionPane.showInputDialog(this,
                "Chọn trạng thái mới:",
                "Cập nhật trạng thái",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statusLabels,
                statusLabels[0]);

        if (selected != null) {
            String newStatus = statuses[java.util.Arrays.asList(statusLabels).indexOf(selected)];
            Response response = apiService.updateOrderStatus(orderId, newStatus);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thành công!");
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
