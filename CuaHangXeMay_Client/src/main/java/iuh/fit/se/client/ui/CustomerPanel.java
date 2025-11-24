package iuh.fit.se.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import iuh.fit.se.client.service.ApiService;
import iuh.fit.se.common.Customer;
import iuh.fit.se.common.Response;
import iuh.fit.se.common.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerPanel extends JPanel {
    private User currentUser;
    private ApiService apiService;
    private Gson gson;

    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnSearch;

    public CustomerPanel(User user) {
        this.currentUser = user;
        this.apiService = new ApiService();
        this.gson = new Gson();
        initComponents();
        loadCustomers();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(52, 73, 94));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setBackground(Color.WHITE);

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(250, 35));
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 14));

        btnSearch = createButton("🔍 Tìm kiếm", new Color(52, 152, 219));
        btnSearch.addActionListener(e -> searchCustomers());

        btnAdd = createButton("➕ Thêm mới", new Color(46, 204, 113));
        btnAdd.addActionListener(e -> showAddDialog());

        btnEdit = createButton("✏️ Sửa", new Color(241, 196, 15));
        btnEdit.addActionListener(e -> showEditDialog());

        btnDelete = createButton("🗑️ Xóa", new Color(231, 76, 60));
        btnDelete.addActionListener(e -> deleteCustomer());

        btnRefresh = createButton("🔄 Làm mới", new Color(149, 165, 166));
        btnRefresh.addActionListener(e -> loadCustomers());

        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        toolbarPanel.add(btnAdd);
        toolbarPanel.add(btnEdit);
        toolbarPanel.add(btnDelete);
        toolbarPanel.add(btnRefresh);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Họ tên", "Email", "Điện thoại", "Địa chỉ", "Thành phố", "Quận/Huyện", "Điểm thưởng", "Tổng chi tiêu"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(tableModel);
        customerTable.setFont(new Font("Arial", Font.PLAIN, 12));
        customerTable.setRowHeight(30);
        customerTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        customerTable.getTableHeader().setBackground(new Color(52, 73, 94));
        customerTable.getTableHeader().setForeground(Color.WHITE);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(customerTable);
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
        return button;
    }

    public void refresh() {
        loadCustomers();
    }

    private void loadCustomers() {
        tableModel.setRowCount(0);

        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws Exception {
                return apiService.getAllCustomers();
            }

            @Override
            protected void done() {
                try {
                    Response response = get();
                    if (response.isSuccess()) {
                        String json = gson.toJson(response.getData());
                        List<Customer> customers = gson.fromJson(json, new TypeToken<List<Customer>>(){}.getType());

                        for (Customer c : customers) {
                            tableModel.addRow(new Object[]{
                                    c.getId(),
                                    c.getFullName(),
                                    c.getEmail(),
                                    c.getPhone(),
                                    c.getAddress(),
                                    c.getCity(),
                                    c.getDistrict(),
                                    c.getLoyaltyPoints(),
                                    String.format("%,.0f đ", c.getTotalSpent())
                            });
                        }
                    } else {
                        JOptionPane.showMessageDialog(CustomerPanel.this,
                                response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CustomerPanel.this,
                            "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void searchCustomers() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadCustomers();
            return;
        }

        tableModel.setRowCount(0);
        Response response = apiService.searchCustomers(keyword);

        if (response.isSuccess()) {
            String json = gson.toJson(response.getData());
            List<Customer> customers = gson.fromJson(json, new TypeToken<List<Customer>>(){}.getType());

            for (Customer c : customers) {
                tableModel.addRow(new Object[]{
                        c.getId(), c.getFullName(), c.getEmail(), c.getPhone(),
                        c.getAddress(), c.getCity(), c.getDistrict(),
                        c.getLoyaltyPoints(), String.format("%,.0f đ", c.getTotalSpent())
                });
            }
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm khách hàng", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField txtFullName = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JTextField txtPhone = new JTextField(20);
        JTextField txtAddress = new JTextField(20);
        JTextField txtCity = new JTextField(20);
        JTextField txtDistrict = new JTextField(20);

        int row = 0;
        addFormField(panel, gbc, row++, "Họ tên:", txtFullName);
        addFormField(panel, gbc, row++, "Email:", txtEmail);
        addFormField(panel, gbc, row++, "Điện thoại:", txtPhone);
        addFormField(panel, gbc, row++, "Địa chỉ:", txtAddress);
        addFormField(panel, gbc, row++, "Thành phố:", txtCity);
        addFormField(panel, gbc, row++, "Quận/Huyện:", txtDistrict);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        btnSave.addActionListener(e -> {
            try {
                Customer customer = new Customer();
                customer.setFullName(txtFullName.getText());
                customer.setEmail(txtEmail.getText());
                customer.setPhone(txtPhone.getText());
                customer.setAddress(txtAddress.getText());
                customer.setCity(txtCity.getText());
                customer.setDistrict(txtDistrict.getText());

                Response response = apiService.createCustomer(customer);
                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Thêm khách hàng thành công!");
                    loadCustomers();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa!");
            return;
        }

        JOptionPane.showMessageDialog(this, "Chức năng đang phát triển!");
    }

    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa khách hàng này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int customerId = (int) tableModel.getValueAt(selectedRow, 0);
            Response response = apiService.deleteCustomer(customerId);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Xóa khách hàng thành công!");
                loadCustomers();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(field, gbc);
    }
}
