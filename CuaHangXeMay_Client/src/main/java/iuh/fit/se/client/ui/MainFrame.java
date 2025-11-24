package iuh.fit.se.client.ui;

import iuh.fit.se.common.User;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private User currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Menu buttons
    private JButton btnDashboard;
    private JButton btnProducts;
    private JButton btnCustomers;
    private JButton btnOrders;
    private JButton btnInventory;
    private JButton btnUsers;
    private JButton btnLogout;

    // Panels
    private DashboardPanel dashboardPanel;
    private ProductPanel productPanel;
    private CustomerPanel customerPanel;
    private OrderPanel orderPanel;
    private InventoryPanel inventoryPanel;
    private UserPanel userPanel;

    public MainFrame(User user) {
        this.currentUser = user;
        initComponents();
        setLocationRelativeTo(null);
        showDashboard();
    }

    private void initComponents() {
        setTitle("Hệ thống Quản lý Cửa hàng Xe máy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setMinimumSize(new Dimension(1200, 700));

        // Main layout
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);

        // Initialize panels
        dashboardPanel = new DashboardPanel(currentUser);
        productPanel = new ProductPanel(currentUser);
        customerPanel = new CustomerPanel(currentUser);
        orderPanel = new OrderPanel(currentUser);
        inventoryPanel = new InventoryPanel(currentUser);
        userPanel = new UserPanel(currentUser);

        // Add panels to content
        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(productPanel, "PRODUCTS");
        contentPanel.add(customerPanel, "CUSTOMERS");
        contentPanel.add(orderPanel, "ORDERS");
        contentPanel.add(inventoryPanel, "INVENTORY");
        contentPanel.add(userPanel, "USERS");

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBackground(new Color(44, 62, 80));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel lblTitle = new JLabel("CỬA HÀNG XE MÁY");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUser = new JLabel("Xin chào, " + currentUser.getFullName());
        lblUser.setFont(new Font("Arial", Font.PLAIN, 12));
        lblUser.setForeground(new Color(189, 195, 199));
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRole = new JLabel("(" + currentUser.getRole() + ")");
        lblRole.setFont(new Font("Arial", Font.ITALIC, 11));
        lblRole.setForeground(new Color(149, 165, 166));
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(lblUser);
        headerPanel.add(lblRole);

        // Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(44, 62, 80));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        btnDashboard = createMenuButton("📊 Tổng quan", "DASHBOARD");
        btnProducts = createMenuButton("🏍️ Sản phẩm", "PRODUCTS");
        btnCustomers = createMenuButton("👥 Khách hàng", "CUSTOMERS");
        btnOrders = createMenuButton("🛒 Đơn hàng", "ORDERS");
        btnInventory = createMenuButton("📦 Tồn kho", "INVENTORY");

        menuPanel.add(btnDashboard);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(btnProducts);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(btnCustomers);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(btnOrders);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(btnInventory);

        // Only show user management for ADMIN
        if ("ADMIN".equals(currentUser.getRole())) {
            menuPanel.add(Box.createVerticalStrut(5));
            btnUsers = createMenuButton("👤 Người dùng", "USERS");
            menuPanel.add(btnUsers);
        }

        menuPanel.add(Box.createVerticalGlue());

        // Logout button
        btnLogout = createMenuButton("🚪 Đăng xuất", "LOGOUT");
        btnLogout.setBackground(new Color(231, 76, 60));
        menuPanel.add(btnLogout);

        sidebar.add(headerPanel, BorderLayout.NORTH);
        sidebar.add(menuPanel, BorderLayout.CENTER);

        return sidebar;
    }

    private JButton createMenuButton(String text, String action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 73, 94));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setPreferredSize(new Dimension(220, 45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!action.equals("LOGOUT")) {
                    button.setBackground(new Color(41, 128, 185));
                } else {
                    button.setBackground(new Color(192, 57, 43));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!action.equals("LOGOUT")) {
                    button.setBackground(new Color(52, 73, 94));
                } else {
                    button.setBackground(new Color(231, 76, 60));
                }
            }
        });

        button.addActionListener(e -> handleMenuAction(action));

        return button;
    }

    private void handleMenuAction(String action) {
        switch (action) {
            case "DASHBOARD":
                showDashboard();
                break;
            case "PRODUCTS":
                showProducts();
                break;
            case "CUSTOMERS":
                showCustomers();
                break;
            case "ORDERS":
                showOrders();
                break;
            case "INVENTORY":
                showInventory();
                break;
            case "USERS":
                showUsers();
                break;
            case "LOGOUT":
                logout();
                break;
        }
    }

    private void showDashboard() {
        cardLayout.show(contentPanel, "DASHBOARD");
        dashboardPanel.refresh();
    }

    private void showProducts() {
        cardLayout.show(contentPanel, "PRODUCTS");
        productPanel.refresh();
    }

    private void showCustomers() {
        cardLayout.show(contentPanel, "CUSTOMERS");
        customerPanel.refresh();
    }

    private void showOrders() {
        cardLayout.show(contentPanel, "ORDERS");
        orderPanel.refresh();
    }

    private void showInventory() {
        cardLayout.show(contentPanel, "INVENTORY");
        inventoryPanel.refresh();
    }

    private void showUsers() {
        cardLayout.show(contentPanel, "USERS");
        userPanel.refresh();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đăng xuất?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
