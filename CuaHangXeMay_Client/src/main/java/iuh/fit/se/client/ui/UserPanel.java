package iuh.fit.se.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import iuh.fit.se.client.service.ApiService;
import iuh.fit.se.common.Response;
import iuh.fit.se.common.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserPanel extends JPanel {
    private User currentUser;
    private ApiService apiService;
    private Gson gson;

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;

    public UserPanel(User user) {
        this.currentUser = user;
        this.apiService = new ApiService();
        this.gson = new Gson();
        initComponents();
        loadUsers();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("QUẢN LÝ NGƯỜI DÙNG");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(52, 73, 94));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setBackground(Color.WHITE);

        btnAdd = createButton("➕ Thêm mới", new Color(46, 204, 113));
        btnAdd.addActionListener(e -> showAddDialog());

        btnEdit = createButton("✏️ Sửa", new Color(241, 196, 15));
        btnEdit.addActionListener(e -> showEditDialog());

        btnDelete = createButton("🗑️ Xóa", new Color(231, 76, 60));
        btnDelete.addActionListener(e -> deleteUser());

        btnRefresh = createButton("🔄 Làm mới", new Color(149, 165, 166));
        btnRefresh.addActionListener(e -> loadUsers());

        toolbarPanel.add(btnAdd);
        toolbarPanel.add(btnEdit);
        toolbarPanel.add(btnDelete);
        toolbarPanel.add(btnRefresh);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Tên đăng nhập", "Họ tên", "Email", "Điện thoại", "Vai trò", "Trạng thái", "Ngày tạo"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setFont(new Font("Arial", Font.PLAIN, 12));
        userTable.setRowHeight(30);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        userTable.getTableHeader().setBackground(new Color(52, 73, 94));
        userTable.getTableHeader().setForeground(Color.WHITE);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(userTable);
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
        loadUsers();
    }

    private void loadUsers() {
        tableModel.setRowCount(0);

        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws Exception {
                return apiService.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    Response response = get();
                    if (response.isSuccess()) {
                        String json = gson.toJson(response.getData());
                        List<User> users = gson.fromJson(json, new TypeToken<List<User>>(){}.getType());

                        for (User u : users) {
                            tableModel.addRow(new Object[]{
                                    u.getId(),
                                    u.getUsername(),
                                    u.getFullName(),
                                    u.getEmail(),
                                    u.getPhone(),
                                    getRoleDisplay(u.getRole()),
                                    getStatusDisplay(u.getStatus()),
                                    u.getCreatedAt()
                            });
                        }
                    } else {
                        JOptionPane.showMessageDialog(UserPanel.this,
                                response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(UserPanel.this,
                            "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private String getRoleDisplay(String role) {
        switch (role) {
            case "ADMIN": return "👑 Quản trị viên";
            case "MANAGER": return "💼 Quản lý";
            case "STAFF": return "👤 Nhân viên";
            default: return role;
        }
    }

    private String getStatusDisplay(String status) {
        return "ACTIVE".equals(status) ? "✅ Hoạt động" : "❌ Không hoạt động";
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm người dùng", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField txtUsername = new JTextField(20);
        JPasswordField txtPassword = new JPasswordField(20);
        JTextField txtFullName = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JTextField txtPhone = new JTextField(20);
        JComboBox<String> cboRole = new JComboBox<>(new String[]{"STAFF", "MANAGER", "ADMIN"});
        JComboBox<String> cboStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        int row = 0;
        addFormField(panel, gbc, row++, "Tên đăng nhập:", txtUsername);
        addFormField(panel, gbc, row++, "Mật khẩu:", txtPassword);
        addFormField(panel, gbc, row++, "Họ tên:", txtFullName);
        addFormField(panel, gbc, row++, "Email:", txtEmail);
        addFormField(panel, gbc, row++, "Điện thoại:", txtPhone);
        addFormField(panel, gbc, row++, "Vai trò:", cboRole);
        addFormField(panel, gbc, row++, "Trạng thái:", cboStatus);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        btnSave.addActionListener(e -> {
            try {
                User user = new User();
                user.setUsername(txtUsername.getText());
                user.setPassword(new String(txtPassword.getPassword()));
                user.setFullName(txtFullName.getText());
                user.setEmail(txtEmail.getText());
                user.setPhone(txtPhone.getText());
                user.setRole((String) cboRole.getSelectedItem());
                user.setStatus((String) cboStatus.getSelectedItem());

                Response response = apiService.createUser(user);
                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Thêm người dùng thành công!");
                    loadUsers();
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
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần sửa!");
            return;
        }

        JOptionPane.showMessageDialog(this, "Chức năng đang phát triển!");
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa người dùng này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            Response response = apiService.deleteUser(userId);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Xóa người dùng thành công!");
                loadUsers();
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
