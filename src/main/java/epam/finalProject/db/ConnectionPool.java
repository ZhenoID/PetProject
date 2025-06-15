package epam.finalProject.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {
    private static final String URL = "jdbc:postgresql://localhost:5432/Library";
    private static final String USER = "postgres";
    private static final String PASSWORD = "12345";
    private static final int POOL_SIZE = 10;

    private final BlockingQueue<Connection> pool = new ArrayBlockingQueue<>(POOL_SIZE);
    private static ConnectionPool instance;
    private static DataSource testDataSource;

    public ConnectionPool() {
        try {
            for (int i = 0; i < POOL_SIZE; i++) {
                Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                pool.offer(new PooledConnection(connection, this));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }

    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (testDataSource != null) {
            return testDataSource.getConnection();
        }

        try {
            return pool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a database connection", e);
        }
    }

    public void releaseConnection(Connection connection) {
        if (connection != null && testDataSource == null) {
            try {
                pool.put(connection);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    public int getAvailableConnections() {
        if (testDataSource != null) {
            return 0;
        } else {
            return pool.size();
        }
    }

    public static void setTestDataSource(DataSource ds) {
        testDataSource = ds;
    }
}
