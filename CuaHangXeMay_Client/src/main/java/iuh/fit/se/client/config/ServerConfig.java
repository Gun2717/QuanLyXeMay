package iuh.fit.se.client.config;

import iuh.fit.se.common.Constants;

public class ServerConfig {
    private static String host = Constants.SERVER_HOST;
    private static int port = Constants.SERVER_PORT;

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        ServerConfig.host = host;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        ServerConfig.port = port;
    }

    public static String getServerAddress() {
        return host + ":" + port;
    }
}
