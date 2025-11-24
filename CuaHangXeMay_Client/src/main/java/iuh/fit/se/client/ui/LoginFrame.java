package iuh.fit.se.client.ui;

import com.formdev.flatlaf.FlatLightLaf;
import iuh.fit.se.client.service.ApiService;
import iuh.fit.se.common.Response;
import iuh.fit.se.common.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private ApiService apiService;

    public LoginFrame() {
        apiService = new ApiService();
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Đăng nhập - Hệ thống Quản lý Cửa hàng Xe máy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(41, 128, 185));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(lblTitle, gbc);

        // Subtitle
        JLabel lblSubtitle = new JLabel("Quản lý Cửa hàng Xe máy");
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitle.setForeground(Color.GRAY);
        lblSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        mainPanel.add(lblSubtitle, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel lblUsername = new JLabel("Tên đăng nhập:");
        lblUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        mainPanel.add(lblUsername, gbc);

        gbc.gridx = 1;
        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(250, 35));
        mainPanel.add(txtUsername, gbc);

        // Password
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        mainPanel.add(lblPassword, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(250, 35));
        mainPanel.add(txtPassword, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(120, 40));
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> login());

        btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Arial", Font.BOLD, 14));
        btnExit.setPreferredSize(new Dimension(120, 40));
        btnExit.setBackground(new Color(231, 76, 60));
        btnExit.setForeground(Color.WHITE);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExit.addActionListener(e -> System.exit(0));

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        mainPanel.add(buttonPanel, gbc);

        // Add enter key listener
        txtPassword.addActionListener(e -> login());

        add(mainPanel);

        // Set default values for testing
        txtUsername.setText("admin");
        txtPassword.setText("admin123");
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ thông tin!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show loading
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws Exception {
                return apiService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    Response response = get();

                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");

                    if (response.isSuccess()) {
                        User user = (User) response.getData();
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Đăng nhập thành công!\nXin chào " + user.getFullName(),
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);

                        // Open main frame in fullscreen
                        SwingUtilities.invokeLater(() -> {
                            MainFrame mainFrame = new MainFrame(user);
                            // Set to maximized state
                            mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                            mainFrame.setVisible(true);
                            dispose();
                        });
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                response.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Lỗi: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
