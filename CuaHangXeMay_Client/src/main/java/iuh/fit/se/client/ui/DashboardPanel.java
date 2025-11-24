package iuh.fit.se.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import iuh.fit.se.client.service.ApiService;
import iuh.fit.se.common.Response;
import iuh.fit.se.common.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private User currentUser;
    private ApiService apiService;
    private Gson gson;

    // Stats labels
    private JLabel lblTotalProducts;
    private JLabel lblTotalCustomers;
    private JLabel lblTodayOrders;
    private JLabel lblTodayRevenue;

    // Chart panel
    private JPanel chartPanel;
    private JComboBox<String> cboTimeRange;
    private JComboBox<String> cboChartType;

    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    public DashboardPanel(User user) {
        this.currentUser = user;
        this.apiService = new ApiService();
        this.gson = new Gson();
        this.currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        initComponents();
        loadDashboardData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("TỔNG QUAN HỆ THỐNG");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitle.setForeground(new Color(52, 73, 94));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JLabel lblWelcome = new JLabel("Xin chào, " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        lblWelcome.setFont(new Font("Arial", Font.PLAIN, 14));
        lblWelcome.setForeground(new Color(127, 140, 141));
        headerPanel.add(lblWelcome, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Stats cards panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setPreferredSize(new Dimension(0, 140));

        statsPanel.add(createStatCard("Tổng sản phẩm", "0", "📦", new Color(52, 152, 219), "totalProducts"));
        statsPanel.add(createStatCard("Khách hàng", "0", "👥", new Color(46, 204, 113), "totalCustomers"));
        statsPanel.add(createStatCard("Đơn hàng hôm nay", "0", "🛒", new Color(241, 196, 15), "todayOrders"));
        statsPanel.add(createStatCard("Doanh thu hôm nay", "0 đ", "💰", new Color(155, 89, 182), "todayRevenue"));

        mainPanel.add(statsPanel, BorderLayout.NORTH);

        // Chart section
        JPanel chartSection = new JPanel(new BorderLayout(10, 10));
        chartSection.setBackground(Color.WHITE);
        chartSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Chart controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlPanel.setBackground(Color.WHITE);

        JLabel lblChartType = new JLabel("Thống kê:");
        lblChartType.setFont(new Font("Arial", Font.BOLD, 13));

        cboChartType = new JComboBox<>(new String[]{
                "Doanh thu", "Đơn hàng", "Khách hàng", "Sản phẩm bán"
        });
        cboChartType.setPreferredSize(new Dimension(150, 35));
        cboChartType.setFont(new Font("Arial", Font.PLAIN, 13));

        JLabel lblTimeRange = new JLabel("Thời gian:");
        lblTimeRange.setFont(new Font("Arial", Font.BOLD, 13));

        cboTimeRange = new JComboBox<>(new String[]{
                "7 ngày gần nhất", "Tháng này", "Tháng trước", "Quý này", "Năm nay"
        });
        cboTimeRange.setPreferredSize(new Dimension(150, 35));
        cboTimeRange.setFont(new Font("Arial", Font.PLAIN, 13));

        JButton btnRefresh = new JButton("🔄 Làm mới");
        btnRefresh.setFont(new Font("Arial", Font.PLAIN, 13));
        btnRefresh.setPreferredSize(new Dimension(120, 35));
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadChartData());

        controlPanel.add(lblChartType);
        controlPanel.add(cboChartType);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(lblTimeRange);
        controlPanel.add(cboTimeRange);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(btnRefresh);

        chartSection.add(controlPanel, BorderLayout.NORTH);

        // Chart panel
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        chartSection.add(chartPanel, BorderLayout.CENTER);

        mainPanel.add(chartSection, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Add listeners
        cboChartType.addActionListener(e -> loadChartData());
        cboTimeRange.addActionListener(e -> loadChartData());
    }

    private JPanel createStatCard(String title, String value, String icon, Color color, String key) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Icon
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblIcon, BorderLayout.WEST);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(127, 140, 141));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.BOLD, 24));
        lblValue.setForeground(color);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Store reference for updating
        if (key.equals("totalProducts")) lblTotalProducts = lblValue;
        else if (key.equals("totalCustomers")) lblTotalCustomers = lblValue;
        else if (key.equals("todayOrders")) lblTodayOrders = lblValue;
        else if (key.equals("todayRevenue")) lblTodayRevenue = lblValue;

        contentPanel.add(lblTitle);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(lblValue);

        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    public void refresh() {
        loadDashboardData();
        loadChartData();
    }

    private void loadDashboardData() {
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                Map<String, Object> stats = new HashMap<>();

                // Get total products
                Response productResponse = apiService.getAllProducts();
                if (productResponse.isSuccess()) {
                    String json = gson.toJson(productResponse.getData());
                    List<?> products = gson.fromJson(json, List.class);
                    stats.put("totalProducts", products.size());
                }

                // Get total customers
                Response customerResponse = apiService.getAllCustomers();
                if (customerResponse.isSuccess()) {
                    String json = gson.toJson(customerResponse.getData());
                    List<?> customers = gson.fromJson(json, List.class);
                    stats.put("totalCustomers", customers.size());
                }

                // Get today's orders and revenue
                Response orderResponse = apiService.getAllOrders();
                if (orderResponse.isSuccess()) {
                    String json = gson.toJson(orderResponse.getData());
                    List<Map<String, Object>> orders = gson.fromJson(json, new TypeToken<List<Map<String, Object>>>(){}.getType());

                    String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    int todayOrderCount = 0;
                    BigDecimal todayRevenue = BigDecimal.ZERO;

                    for (Map<String, Object> order : orders) {
                        String createdAt = (String) order.get("createdAt");
                        if (createdAt != null && createdAt.startsWith(today)) {
                            String status = (String) order.get("status");
                            if ("COMPLETED".equals(status)) {
                                todayOrderCount++;
                                Object finalAmount = order.get("finalAmount");
                                if (finalAmount instanceof Number) {
                                    todayRevenue = todayRevenue.add(new BigDecimal(finalAmount.toString()));
                                }
                            }
                        }
                    }

                    stats.put("todayOrders", todayOrderCount);
                    stats.put("todayRevenue", todayRevenue);
                }

                return stats;
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> stats = get();

                    if (stats.containsKey("totalProducts")) {
                        lblTotalProducts.setText(String.valueOf(stats.get("totalProducts")));
                    }
                    if (stats.containsKey("totalCustomers")) {
                        lblTotalCustomers.setText(String.valueOf(stats.get("totalCustomers")));
                    }
                    if (stats.containsKey("todayOrders")) {
                        lblTodayOrders.setText(String.valueOf(stats.get("todayOrders")));
                    }
                    if (stats.containsKey("todayRevenue")) {
                        BigDecimal revenue = (BigDecimal) stats.get("todayRevenue");
                        lblTodayRevenue.setText(currencyFormat.format(revenue) + " đ");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                            "Lỗi tải dữ liệu: " + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadChartData() {
        String chartType = (String) cboChartType.getSelectedItem();
        String timeRange = (String) cboTimeRange.getSelectedItem();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Response orderResponse = apiService.getAllOrders();
                if (!orderResponse.isSuccess()) {
                    return null;
                }

                String json = gson.toJson(orderResponse.getData());
                List<Map<String, Object>> orders = gson.fromJson(json, new TypeToken<List<Map<String, Object>>>(){}.getType());

                // Filter orders by time range
                List<Map<String, Object>> filteredOrders = filterOrdersByTimeRange(orders, timeRange);

                // Calculate statistics
                Map<String, Object> chartData = calculateChartData(filteredOrders, chartType, timeRange);

                // Update UI
                SwingUtilities.invokeLater(() -> updateChart(chartData, chartType));

                return null;
            }
        };
        worker.execute();
    }

    private List<Map<String, Object>> filterOrdersByTimeRange(List<Map<String, Object>> orders, String timeRange) {
        List<Map<String, Object>> filtered = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        Date now = new Date();

        for (Map<String, Object> order : orders) {
            try {
                String status = (String) order.get("status");
                if (!"COMPLETED".equals(status)) continue;

                String createdAt = (String) order.get("createdAt");
                if (createdAt == null) continue;

                Date orderDate = new SimpleDateFormat("yyyy-MM-dd").parse(createdAt.substring(0, 10));

                boolean include = false;
                switch (timeRange) {
                    case "7 ngày gần nhất":
                        cal.setTime(now);
                        cal.add(Calendar.DAY_OF_MONTH, -7);
                        include = orderDate.after(cal.getTime());
                        break;
                    case "Tháng này":
                        cal.setTime(now);
                        int currentMonth = cal.get(Calendar.MONTH);
                        int currentYear = cal.get(Calendar.YEAR);
                        cal.setTime(orderDate);
                        include = cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear;
                        break;
                    case "Tháng trước":
                        cal.setTime(now);
                        cal.add(Calendar.MONTH, -1);
                        int lastMonth = cal.get(Calendar.MONTH);
                        int lastMonthYear = cal.get(Calendar.YEAR);
                        cal.setTime(orderDate);
                        include = cal.get(Calendar.MONTH) == lastMonth && cal.get(Calendar.YEAR) == lastMonthYear;
                        break;
                    case "Quý này":
                        cal.setTime(now);
                        int currentQuarter = cal.get(Calendar.MONTH) / 3;
                        int quarterYear = cal.get(Calendar.YEAR);
                        cal.setTime(orderDate);
                        include = (cal.get(Calendar.MONTH) / 3) == currentQuarter && cal.get(Calendar.YEAR) == quarterYear;
                        break;
                    case "Năm nay":
                        cal.setTime(now);
                        int year = cal.get(Calendar.YEAR);
                        cal.setTime(orderDate);
                        include = cal.get(Calendar.YEAR) == year;
                        break;
                }

                if (include) {
                    filtered.add(order);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return filtered;
    }

    private Map<String, Object> calculateChartData(List<Map<String, Object>> orders, String chartType, String timeRange) {
        Map<String, Object> data = new HashMap<>();
        Map<String, BigDecimal> revenueByDate = new TreeMap<>();
        Map<String, Integer> ordersByDate = new TreeMap<>();
        Map<String, Set<String>> customersByDate = new TreeMap<>();
        Map<String, Integer> productsByDate = new TreeMap<>();

        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM");

        for (Map<String, Object> order : orders) {
            try {
                String createdAt = (String) order.get("createdAt");
                Date orderDate = new SimpleDateFormat("yyyy-MM-dd").parse(createdAt.substring(0, 10));
                String dateKey = outputFormat.format(orderDate);

                // Revenue
                Object finalAmount = order.get("finalAmount");
                if (finalAmount instanceof Number) {
                    BigDecimal amount = new BigDecimal(finalAmount.toString());
                    revenueByDate.put(dateKey, revenueByDate.getOrDefault(dateKey, BigDecimal.ZERO).add(amount));
                }

                // Orders
                ordersByDate.put(dateKey, ordersByDate.getOrDefault(dateKey, 0) + 1);

                // Customers
                Object customerName = order.get("customerName");
                if (customerName != null) {
                    customersByDate.putIfAbsent(dateKey, new HashSet<>());
                    customersByDate.get(dateKey).add(customerName.toString());
                }

                // Products (simplified - count order items)
                productsByDate.put(dateKey, productsByDate.getOrDefault(dateKey, 0) + 1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        data.put("revenueByDate", revenueByDate);
        data.put("ordersByDate", ordersByDate);
        data.put("customersByDate", customersByDate);
        data.put("productsByDate", productsByDate);

        return data;
    }

    private void updateChart(Map<String, Object> chartData, String chartType) {
        chartPanel.removeAll();

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);

        String[] columns;
        DefaultTableModel model;

        switch (chartType) {
            case "Doanh thu":
                columns = new String[]{"Ngày", "Doanh thu"};
                model = new DefaultTableModel(columns, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                Map<String, BigDecimal> revenueData = (Map<String, BigDecimal>) chartData.get("revenueByDate");
                BigDecimal totalRevenue = BigDecimal.ZERO;
                for (Map.Entry<String, BigDecimal> entry : revenueData.entrySet()) {
                    model.addRow(new Object[]{
                            entry.getKey(),
                            currencyFormat.format(entry.getValue()) + " đ"
                    });
                    totalRevenue = totalRevenue.add(entry.getValue());
                }

                // Add total row
                model.addRow(new Object[]{"TỔNG", currencyFormat.format(totalRevenue) + " đ"});
                break;

            case "Đơn hàng":
                columns = new String[]{"Ngày", "Số đơn hàng"};
                model = new DefaultTableModel(columns, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                Map<String, Integer> ordersData = (Map<String, Integer>) chartData.get("ordersByDate");
                int totalOrders = 0;
                for (Map.Entry<String, Integer> entry : ordersData.entrySet()) {
                    model.addRow(new Object[]{entry.getKey(), entry.getValue()});
                    totalOrders += entry.getValue();
                }
                model.addRow(new Object[]{"TỔNG", totalOrders});
                break;

            case "Khách hàng":
                columns = new String[]{"Ngày", "Số khách hàng"};
                model = new DefaultTableModel(columns, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                Map<String, Set<String>> customersData = (Map<String, Set<String>>) chartData.get("customersByDate");
                Set<String> allCustomers = new HashSet<>();
                for (Map.Entry<String, Set<String>> entry : customersData.entrySet()) {
                    model.addRow(new Object[]{entry.getKey(), entry.getValue().size()});
                    allCustomers.addAll(entry.getValue());
                }
                model.addRow(new Object[]{"TỔNG", allCustomers.size()});
                break;

            case "Sản phẩm bán":
                columns = new String[]{"Ngày", "Số sản phẩm"};
                model = new DefaultTableModel(columns, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                Map<String, Integer> productsData = (Map<String, Integer>) chartData.get("productsByDate");
                int totalProducts = 0;
                for (Map.Entry<String, Integer> entry : productsData.entrySet()) {
                    model.addRow(new Object[]{entry.getKey(), entry.getValue()});
                    totalProducts += entry.getValue();
                }
                model.addRow(new Object[]{"TỔNG", totalProducts});
                break;

            default:
                columns = new String[]{"Ngày", "Giá trị"};
                model = new DefaultTableModel(columns, 0);
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);

        // Highlight total row
        if (model.getRowCount() > 0) {
            table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (row == table.getRowCount() - 1) {
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                        c.setBackground(new Color(236, 240, 241));
                    } else {
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                        c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                    }
                    return c;
                }
            });
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        chartPanel.add(tablePanel, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
}
