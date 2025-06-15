package epam.finalProject.testUtils;

import epam.finalProject.db.ConnectionPool;

import javax.sql.DataSource;
import java.lang.reflect.Field;

/**
 * Binds a DataSource into the singleton ConnectionPool for tests.
 */
public class ConnectionPoolTestUtils {
    public static void bind(DataSource ds) {
        ConnectionPool.setTestDataSource(ds);
    }
}

