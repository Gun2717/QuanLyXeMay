package iuh.fit.se.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import iuh.fit.se.client.service.ApiService;
import iuh.fit.se.common.Category;
import iuh.fit.se.common.Product;
import iuh.fit.se.common.Response;
import iuh.fit.se.common.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductPanel extends JPanel {
    private User currentUser;
    private ApiService apiService;
    private Gson gson;

    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnSearch;
    private List<Category> categories;
    private List<Product> currentProducts;

    public ProductPanel(User user) {
        this.currentUser = user;
        this.apiService = new ApiService();
        this.gson = new Gson();
        initComponents();
        loadCategories();
        loadProducts();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel lblTitle = new JLabel("QUẢN LÝ SẢN PHẨM");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(52, 73, 94));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setBackground(Color.WHITE);

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(250, 35));
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        txtSearch.addActionListener(e -> searchProducts());

        btnSearch = createButton("🔍 Tìm kiếm", new Color(52, 152, 219));
        btnSearch.addActionListener(e -> searchProducts());

        btnAdd = createButton("➕ Thêm mới", new Color(46, 204, 113));
        btnAdd.addActionListener(e -> showAddDialog());

        btnEdit = createButton("✏️ Sửa", new Color(241, 196, 15));
        btnEdit.addActionListener(e -> showEditDialog());

        btnDelete = createButton("🗑️ Xóa", new Color(231, 76, 60));
        btnDelete.addActionListener(e -> deleteProduct());

        btnRefresh = createButton("🔄 Làm mới", new Color(149, 165, 166));
        btnRefresh.addActionListener(e -> loadProducts());

        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        toolbarPanel.add(btnAdd);
        toolbarPanel.add(btnEdit);
        toolbarPanel.add(btnDelete);
        toolbarPanel.add(btnRefresh);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Tên sản phẩm", "Hãng", "Model", "Màu sắc", "Giá", "Số lượng", "Danh mục", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        productTable.setFont(new Font("Arial", Font.PLAIN, 13));
        productTable.setRowHeight(30);
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        productTable.getTableHeader().setBackground(new Color(52, 73, 94));
        productTable.getTableHeader().setForeground(Color.WHITE);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setSelectionBackground(new Color(52, 152, 219));
        productTable.setSelectionForeground(Color.WHITE);

        // Column widths
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(productTable);
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
        button.setPreferredSize(new Dimension(120, 35));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    public void refresh() {
        loadProducts();
    }

    private void loadCategories() {
        Response response = apiService.getAllCategories();
        if (response.isSuccess()) {
            String json = gson.toJson(response.getData());
            categories = gson.fromJson(json, new TypeToken<List<Category>>(){}.getType());
        }
    }

    private void loadProducts() {
        tableModel.setRowCount(0);

        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws Exception {
                return apiService.getAllProducts();
            }

            @Override
            protected void done() {
                try {
                    Response response = get();
                    if (response.isSuccess()) {
                        String json = gson.toJson(response.getData());
                        currentProducts = gson.fromJson(json, new TypeToken<List<Product>>(){}.getType());

                        for (Product p : currentProducts) {
                            tableModel.addRow(new Object[]{
                                    p.getId(),
                                    p.getName(),
                                    p.getBrand(),
                                    p.getModel(),
                                    p.getColor(),
                                    String.format("%,.0f đ", p.getPrice()),
                                    p.getQuantity(),
                                    p.getCategoryName(),
                                    p.getStatus()
                            });
                        }
                    } else {
                        JOptionPane.showMessageDialog(ProductPanel.this,
                                response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ProductPanel.this,
                            "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void searchProducts() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadProducts();
            return;
        }

        tableModel.setRowCount(0);
        Response response = apiService.searchProducts(keyword);

        if (response.isSuccess()) {
            String json = gson.toJson(response.getData());
            currentProducts = gson.fromJson(json, new TypeToken<List<Product>>(){}.getType());

            for (Product p : currentProducts) {
                tableModel.addRow(new Object[]{
                        p.getId(), p.getName(), p.getBrand(), p.getModel(),
                        p.getColor(), String.format("%,.0f đ", p.getPrice()),
                        p.getQuantity(), p.getCategoryName(), p.getStatus()
                });
            }
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm sản phẩm mới", true);
        dialog.setSize(550, 650);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Form fields
        JComboBox<String> cboCategory = new JComboBox<>();
        for (Category cat : categories) {
            cboCategory.addItem(cat.getName());
        }

        JTextField txtName = new JTextField(25);
        JTextField txtModel = new JTextField(25);
        JTextField txtBrand = new JTextField(25);
        JTextField txtColor = new JTextField(25);
        JTextField txtPrice = new JTextField(25);
        JTextField txtQuantity = new JTextField(25);
        JTextArea txtDescription = new JTextArea(4, 25);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);

        JComboBox<String> cboStatus = new JComboBox<>(new String[]{"AVAILABLE", "OUT_OF_STOCK", "DISCONTINUED"});

        // Add components
        int row = 0;
        addFormField(formPanel, gbc, row++, "Danh mục (*): ", cboCategory);
        addFormField(formPanel, gbc, row++, "Tên sản phẩm (*): ", txtName);
        addFormField(formPanel, gbc, row++, "Model: ", txtModel);
        addFormField(formPanel, gbc, row++, "Hãng (*): ", txtBrand);
        addFormField(formPanel, gbc, row++, "Màu sắc: ", txtColor);
        addFormField(formPanel, gbc, row++, "Giá (*): ", txtPrice);
        addFormField(formPanel, gbc, row++, "Số lượng (*): ", txtQuantity);
        addFormField(formPanel, gbc, row++, "Trạng thái: ", cboStatus);
        addFormField(formPanel, gbc, row++, "Mô tả: ", scrollDesc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton btnSave = createButton("💾 Lưu", new Color(46, 204, 113));
        JButton btnCancel = createButton("❌ Hủy", new Color(231, 76, 60));

        btnSave.addActionListener(e -> {
            try {
                if (txtName.getText().trim().isEmpty() || txtBrand.getText().trim().isEmpty() ||
                        txtPrice.getText().trim().isEmpty() || txtQuantity.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin bắt buộc (*)", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Product product = new Product();
                product.setCategoryId(categories.get(cboCategory.getSelectedIndex()).getId());
                product.setName(txtName.getText().trim());
                product.setModel(txtModel.getText().trim());
                product.setBrand(txtBrand.getText().trim());
                product.setColor(txtColor.getText().trim());
                product.setPrice(new BigDecimal(txtPrice.getText().trim()));
                product.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));
                product.setStatus((String) cboStatus.getSelectedItem());
                product.setDescription(txtDescription.getText().trim());

                Response response = apiService.createProduct(product);
                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Thêm sản phẩm thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadProducts();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá và số lượng phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        Product selectedProduct = currentProducts.stream()
                .filter(p -> p.getId() == productId)
                .findFirst()
                .orElse(null);

        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa thông tin sản phẩm", true);
        dialog.setSize(550, 650);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Form fields with pre-filled data
        JComboBox<String> cboCategory = new JComboBox<>();
        int selectedCategoryIndex = 0;
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            cboCategory.addItem(cat.getName());
            if (cat.getId() == selectedProduct.getCategoryId()) {
                selectedCategoryIndex = i;
            }
        }
        cboCategory.setSelectedIndex(selectedCategoryIndex);

        JTextField txtName = new JTextField(selectedProduct.getName(), 25);
        JTextField txtModel = new JTextField(selectedProduct.getModel(), 25);
        JTextField txtBrand = new JTextField(selectedProduct.getBrand(), 25);
        JTextField txtColor = new JTextField(selectedProduct.getColor(), 25);
        JTextField txtPrice = new JTextField(selectedProduct.getPrice().toString(), 25);
        JTextField txtQuantity = new JTextField(String.valueOf(selectedProduct.getQuantity()), 25);
        JTextArea txtDescription = new JTextArea(selectedProduct.getDescription(), 4, 25);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);

        JComboBox<String> cboStatus = new JComboBox<>(new String[]{"AVAILABLE", "OUT_OF_STOCK", "DISCONTINUED"});
        cboStatus.setSelectedItem(selectedProduct.getStatus());

        // Add components
        int row = 0;
        addFormField(formPanel, gbc, row++, "Danh mục (*): ", cboCategory);
        addFormField(formPanel, gbc, row++, "Tên sản phẩm (*): ", txtName);
        addFormField(formPanel, gbc, row++, "Model: ", txtModel);
        addFormField(formPanel, gbc, row++, "Hãng (*): ", txtBrand);
        addFormField(formPanel, gbc, row++, "Màu sắc: ", txtColor);
        addFormField(formPanel, gbc, row++, "Giá (*): ", txtPrice);
        addFormField(formPanel, gbc, row++, "Số lượng (*): ", txtQuantity);
        addFormField(formPanel, gbc, row++, "Trạng thái: ", cboStatus);
        addFormField(formPanel, gbc, row++, "Mô tả: ", scrollDesc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton btnUpdate = createButton("💾 Cập nhật", new Color(46, 204, 113));
        JButton btnCancel = createButton("❌ Hủy", new Color(231, 76, 60));

        btnUpdate.addActionListener(e -> {
            try {
                if (txtName.getText().trim().isEmpty() || txtBrand.getText().trim().isEmpty() ||
                        txtPrice.getText().trim().isEmpty() || txtQuantity.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin bắt buộc (*)", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                selectedProduct.setCategoryId(categories.get(cboCategory.getSelectedIndex()).getId());
                selectedProduct.setName(txtName.getText().trim());
                selectedProduct.setModel(txtModel.getText().trim());
                selectedProduct.setBrand(txtBrand.getText().trim());
                selectedProduct.setColor(txtColor.getText().trim());
                selectedProduct.setPrice(new BigDecimal(txtPrice.getText().trim()));
                selectedProduct.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));
                selectedProduct.setStatus((String) cboStatus.getSelectedItem());
                selectedProduct.setDescription(txtDescription.getText().trim());

                Response response = apiService.updateProduct(selectedProduct);
                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật sản phẩm thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadProducts();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá và số lượng phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnUpdate);
        btnPanel.add(btnCancel);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa sản phẩm này?\nThao tác này không thể hoàn tác!",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int productId = (int) tableModel.getValueAt(selectedRow, 0);
            Response response = apiService.deleteProduct(productId);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Xóa sản phẩm thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(field, gbc);
    }
}
