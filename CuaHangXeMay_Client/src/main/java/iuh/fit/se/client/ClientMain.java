package iuh.fit.se.client;

import com.formdev.flatlaf.FlatLightLaf;
import iuh.fit.se.client.network.ClientSocket;
import iuh.fit.se.client.ui.LoginFrame;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        // Set FlatLaf Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            // Customize UI defaults
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);

        } catch (Exception e) {
            System.err.println("Failed to initialize LaF");
            e.printStackTrace();
        }

        // Start application
        SwingUtilities.invokeLater(() -> {
            // Test connection
            ClientSocket clientSocket = ClientSocket.getInstance();
            if (clientSocket.connect()) {
                System.out.println("=================================");
                System.out.println("Connected to server successfully!");
                System.out.println("=================================");
            } else {
                int retry = JOptionPane.showConfirmDialog(null,
                        "Không thể kết nối đến server.\nBạn có muốn thử lại?",
                        "Lỗi kết nối",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE);

                if (retry == JOptionPane.YES_OPTION) {
                    clientSocket.reconnect();
                } else {
                    System.exit(0);
                }
            }

            // Show login frame
            new LoginFrame().setVisible(true);
        });

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down client...");
            ClientSocket.getInstance().disconnect();
        }));
    }
}
