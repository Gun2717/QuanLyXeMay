package iuh.fit.se.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import iuh.fit.se.client.service.ApiService;
import iuh.fit.se.common.Response;
import iuh.fit.se.common.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class InventoryPanel extends JPanel {
    private User currentUser;
    private ApiService apiService;
    private Gson gson;

    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JButton btnRefresh, btnImport, btnExport, btnLowStock;

    public InventoryPanel(User user) {
        this.currentUser = user;
        this.apiService = new ApiService();
        this.gson = new Gson();
        initComponents();
        loadInventory();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("QUẢN LÝ TỒN KHO");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(52, 73, 94));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setBackground(Color.WHITE);

        btnImport = createButton("📥 Nhập kho", new Color(46, 204, 113));
        btnImport.addActionListener(e -> showImportDialog());

        btnExport = createButton("📤 Xuất kho", new Color(241, 196, 15));
        btnExport.addActionListener(e -> showExportDialog());

        btnLowStock = createButton("⚠️ Sắp hết hàng", new Color(231, 76, 60));
        btnLowStock.addActionListener(e -> showLowStockProducts());

        btnRefresh = createButton("🔄 Làm mới", new Color(149, 165, 166));
        btnRefresh.addActionListener(e -> loadInventory());

        toolbarPanel.add(btnImport);
        toolbarPanel.add(btnExport);
        toolbarPanel.add(btnLowStock);
        toolbarPanel.add(btnRefresh);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Sản phẩm", "Hãng", "Model", "Nhập kho", "Xuất kho", "Tồn hiện tại", "Trạng thái", "Cập nhật"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setFont(new Font("Arial", Font.PLAIN, 12));
        inventoryTable.setRowHeight(30);
        inventoryTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        inventoryTable.getTableHeader().setBackground(new Color(52, 73, 94));
        inventoryTable.getTableHeader().setForeground(Color.WHITE);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        add(scrollPane, BorderLayout.CENTER);

        // Statistics Panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createTitledBorder("Thống kê"));

        JLabel lblTotalProducts = new JLabel("Tổng sản phẩm: 0");
        JLabel lblTotalStock = new JLabel("Tổng tồn kho: 0");
        JLabel lblLowStock = new JLabel("Sắp hết hàng: 0");

        lblTotalProducts.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTotalStock.setFont(new Font("Arial", Font.PLAIN, 14));
        lblLowStock.setFont(new Font("Arial", Font.PLAIN, 14));
        lblLowStock.setForeground(new Color(231, 76, 60));

        statsPanel.add(lblTotalProducts);
        statsPanel.add(lblTotalStock);
        statsPanel.add(lblLowStock);

        add(statsPanel, BorderLayout.SOUTH);
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
        loadInventory();
    }

    private void loadInventory() {
        tableModel.setRowCount(0);

        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws Exception {
                return apiService.getAllInventory();
            }

            @Override
            protected void done() {
                try {
                    Response response = get();
                    if (response.isSuccess()) {
                        String json = gson.toJson(response.getData());
                        List<Map<String, Object>> inventory = gson.fromJson(json,
                                new TypeToken<List<Map<String, Object>>>(){}.getType());

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        int totalStock = 0;
                        int lowStockCount = 0;

                        for (Map<String, Object> item : inventory) {
                            int currentQty = ((Double) item.get("quantityCurrent")).intValue();
                            totalStock += currentQty;
                            if (currentQty < 10) lowStockCount++;

                            String status = currentQty < 5 ? "⚠️ Cần nhập" :
                                    currentQty < 10 ? "⚡ Sắp hết" : "✅ Đủ hàng";

                            tableModel.addRow(new Object[]{
                                    ((Double) item.get("id")).intValue(),
                                    item.get("productName"),
                                    item.get("brand"),
                                    item.get("model"),
                                    ((Double) item.get("quantityIn")).intValue(),
                                    ((Double) item.get("quantityOut")).intValue(),
                                    currentQty,
                                    status,
                                    sdf.format(new java.util.Date((String) item.get("lastUpdated")))
                            });
                        }

                        // Update statistics
                        updateStatistics(inventory.size(), totalStock, lowStockCount);
                    } else {
                        JOptionPane.showMessageDialog(InventoryPanel.this,
                                response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(InventoryPanel.this,
                            "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateStatistics(int totalProducts, int totalStock, int lowStock) {
        Component[] components = ((JPanel) getComponent(2)).getComponents();
        ((JLabel) components[0]).setText("Tổng sản phẩm: " + totalProducts);
        ((JLabel) components[1]).setText("Tổng tồn kho: " + totalStock);
        ((JLabel) components[2]).setText("Sắp hết hàng: " + lowStock);
    }

    private void showImportDialog() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn sản phẩm cần nhập kho!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 1);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblProduct = new JLabel("Sản phẩm:");
        JLabel lblProductValue = new JLabel(productName);
        lblProductValue.setFont(new Font("Arial", Font.BOLD, 13));

        JLabel lblQuantity = new JLabel("Số lượng nhập:");
        JSpinner spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spinQuantity.setFont(new Font("Arial", Font.PLAIN, 13));

        panel.add(lblProduct);
        panel.add(lblProductValue);
        panel.add(lblQuantity);
        panel.add(spinQuantity);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Nhập kho", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int quantity = (int) spinQuantity.getValue();
            Response response = apiService.updateInventory(productId, quantity, "IN");

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Nhập kho thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadInventory();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showExportDialog() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn sản phẩm cần xuất kho!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 1);
        int currentStock = (int) tableModel.getValueAt(selectedRow, 6);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblProduct = new JLabel("Sản phẩm:");
        JLabel lblProductValue = new JLabel(productName);
        lblProductValue.setFont(new Font("Arial", Font.BOLD, 13));

        JLabel lblCurrent = new JLabel("Tồn hiện tại:");
        JLabel lblCurrentValue = new JLabel(String.valueOf(currentStock));
        lblCurrentValue.setFont(new Font("Arial", Font.BOLD, 13));
        lblCurrentValue.setForeground(new Color(52, 152, 219));

        JLabel lblQuantity = new JLabel("Số lượng xuất:");
        JSpinner spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, currentStock, 1));
        spinQuantity.setFont(new Font("Arial", Font.PLAIN, 13));

        panel.add(lblProduct);
        panel.add(lblProductValue);
        panel.add(lblCurrent);
        panel.add(lblCurrentValue);
        panel.add(lblQuantity);
        panel.add(spinQuantity);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Xuất kho", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int quantity = (int) spinQuantity.getValue();
            Response response = apiService.updateInventory(productId, quantity, "OUT");

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Xuất kho thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadInventory();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showLowStockProducts() {
        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws Exception {
                return apiService.getLowStockProducts();
            }

            @Override
            protected void done() {
                try {
                    Response response = get();
                    if (response.isSuccess()) {
                        String json = gson.toJson(response.getData());
                        List<Map<String, Object>> products = gson.fromJson(json,
                                new TypeToken<List<Map<String, Object>>>(){}.getType());

                        if (products.isEmpty()) {
                            JOptionPane.showMessageDialog(InventoryPanel.this,
                                    "Không có sản phẩm nào sắp hết hàng!",
                                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }

                        // Create dialog to show low stock products
                        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(InventoryPanel.this),
                                "Sản phẩm sắp hết hàng", true);
                        dialog.setSize(700, 400);
                        dialog.setLocationRelativeTo(InventoryPanel.this);

                        String[] cols = {"ID", "Tên sản phẩm", "Hãng", "Model", "Tồn kho", "Giá"};
                        DefaultTableModel model = new DefaultTableModel(cols, 0) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false;
                            }
                        };

                        for (Map<String, Object> p : products) {
                            model.addRow(new Object[]{
                                    ((Double) p.get("id")).intValue(),
                                    p.get("name"),
                                    p.get("brand"),
                                    p.get("model"),
                                    ((Double) p.get("quantityCurrent")).intValue(),
                                    String.format("%,.0f đ", ((Double) p.get("price")).doubleValue())
                            });
                        }

                        JTable table = new JTable(model);
                        table.setRowHeight(25);
                        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
                        table.getTableHeader().setBackground(new Color(52, 73, 94));
                        table.getTableHeader().setForeground(Color.WHITE);

                        JPanel panel = new JPanel(new BorderLayout(10, 10));
                        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

                        JLabel lblWarning = new JLabel("⚠️ Có " + products.size() + " sản phẩm cần nhập kho!");
                        lblWarning.setFont(new Font("Arial", Font.BOLD, 14));
                        lblWarning.setForeground(new Color(231, 76, 60));

                        panel.add(lblWarning, BorderLayout.NORTH);
                        panel.add(new JScrollPane(table), BorderLayout.CENTER);

                        dialog.add(panel);
                        dialog.setVisible(true);

                    } else {
                        JOptionPane.showMessageDialog(InventoryPanel.this,
                                response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(InventoryPanel.this,
                            "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
