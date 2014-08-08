import connection.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class Runner {
    public static void main(String[] args) throws SQLException {
        Logger log = LoggerFactory.getLogger(Runner.class);
        ConnectionPool.CPConfig config = new ConnectionPool.CPConfig();
        config
                .setMaxConn(10)
                .setPassword("")
                .setUrl("jdbc:h2:tcp://localhost/c:/db/test")
                .setUser("sa");
        ConnectionPool.setConfig(config);
        ConnectionPool connectionPool = ConnectionPool.getInstance();
        Connection con1 = connectionPool.getConnection();
        Connection con2 = connectionPool.getConnection();
    }
}
