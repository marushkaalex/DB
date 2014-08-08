package connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ConnectionPool {
    private static final Logger log = LoggerFactory.getLogger(ConnectionPool.class);
    private static ConnectionPool instance;
    private static List<Connection> freeConnections = new LinkedList<Connection>();
    private static CPConfig config;

    private ConnectionPool() {
    }

    public static CPConfig getConfig() {
        return config;
    }

    public static synchronized void setConfig(CPConfig config) {
        checkConfig(config);
        ConnectionPool.config = config;
    }

    public static synchronized ConnectionPool getInstance() {
        checkConfig(ConnectionPool.config);
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    private static void checkConfig(CPConfig config) {
        if (config == null) throw new ConnectionException("You must set connection pull configuration");
        if (config.url == null ||
                config.user == null ||
                config.password == null) {
            throw new ConnectionException("You must declare all CPConfig fields\n" + config.toString());
        }
    }

    public synchronized Connection getConnection() {
        Connection con = null;
        if (!freeConnections.isEmpty()) {
            con = freeConnections.get(0);
            freeConnections.remove(con);
            try {
                if (con.isClosed()) {
                    con = getConnection();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            con = newConnection();
        }
        return con;
    }

    private Connection newConnection() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(config.url, config.user, config.password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    public synchronized void freeConnection(Connection con) {
        if ((con != null) && (freeConnections.size() < config.maxConn)) {
            freeConnections.add(con);
        }
    }

    public synchronized void release() {
        for (Connection freeConnection : freeConnections) {
            try {
                freeConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        freeConnections.clear();
    }

    public static class CPConfig {
        private String url;
        private String user;
        private String password;
        private int maxConn;

        public CPConfig setMaxConn(int maxConn) {
            if (maxConn < 1) throw new IllegalArgumentException("There must be more than 0 connections");
            this.maxConn = maxConn;
            return this;
        }

        public CPConfig setUrl(String url) {
            this.url = url;
            return this;
        }

        public CPConfig setUser(String user) {
            this.user = user;
            return this;
        }

        public CPConfig setPassword(String password) {
            this.password = password;
            return this;
        }

        @Override
        public String toString() {
            return "CPConfig{" +
                    ", url='" + url + '\'' +
                    ", user='" + user + '\'' +
                    ", password='" + password + '\'' +
                    ", maxConn=" + maxConn +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CPConfig config = (CPConfig) o;

            if (maxConn != config.maxConn) return false;
            if (password != null ? !password.equals(config.password) : config.password != null) return false;
            if (url != null ? !url.equals(config.url) : config.url != null) return false;
            if (user != null ? !user.equals(config.user) : config.user != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = url != null ? url.hashCode() : 0;
            result = 31 * result + (user != null ? user.hashCode() : 0);
            result = 31 * result + (password != null ? password.hashCode() : 0);
            result = 31 * result + maxConn;
            return result;
        }
    }
}
