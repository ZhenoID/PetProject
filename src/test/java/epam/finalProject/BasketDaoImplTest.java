package epam.finalProject;

import epam.finalProject.DAO.BasketDaoImpl;
import epam.finalProject.db.ConnectionPool;
import epam.finalProject.entity.BasketItem;
import epam.finalProject.testUtils.ConnectionPoolTestUtils;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.StringReader;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

    class BasketDaoImplTest {

        private BasketDaoImpl dao;

        @BeforeEach
        void setUp() throws Exception {
            var ds = new DriverManagerDataSource("jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");

            try (Connection conn = ds.getConnection()) {

                conn.createStatement().execute("DROP ALL OBJECTS");
                String schema = """
                        CREATE TABLE basket_items (
                          id SERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          book_id BIGINT NOT NULL,
                          quantity INT NOT NULL
                        );
                        """;
                RunScript.execute(conn, new StringReader(schema));
            }

            ConnectionPool.setTestDataSource(ds);

            dao = new BasketDaoImpl();
        }


        @Test
        void addOrUpdateQuantity_insertNew() {
            boolean ok = dao.addOrUpdateQuantity(1L, 10L, 3);
            assertTrue(ok);
            List<BasketItem> items = dao.findByUserId(1L);
            assertEquals(1, items.size());
            assertEquals(3, items.get(0).getQuantity());
        }

        @Test
        void addOrUpdateQuantity_updatePositive() {
            dao.addOrUpdateQuantity(1L, 10L, 2);
            boolean ok = dao.addOrUpdateQuantity(1L, 10L, 5);
            assertTrue(ok);
            assertEquals(7, dao.findByUserId(1L).get(0).getQuantity());
        }

        @Test
        void addOrUpdateQuantity_decreaseNotDelete() {
            dao.addOrUpdateQuantity(1L, 10L, 5);
            boolean ok = dao.addOrUpdateQuantity(1L, 10L, -3);
            assertTrue(ok);
            assertEquals(2, dao.findByUserId(1L).get(0).getQuantity());
        }

        @Test
        void addOrUpdateQuantity_decreaseToZeroDeletes() {
            dao.addOrUpdateQuantity(1L, 10L, 1);
            boolean ok = dao.addOrUpdateQuantity(1L, 10L, -1);
            assertTrue(ok);
            assertTrue(dao.findByUserId(1L).isEmpty());
        }

        @Test
        void setQuantity_positiveUpdates() {
            dao.addOrUpdateQuantity(2L, 20L, 4);
            boolean ok = dao.setQuantity(2L, 20L, 7);
            assertTrue(ok);
            assertEquals(7, dao.findByUserId(2L).get(0).getQuantity());
        }

        @Test
        void setQuantity_zeroDeletes() {
            dao.addOrUpdateQuantity(3L, 30L, 2);
            boolean ok = dao.setQuantity(3L, 30L, 0);
            assertTrue(ok);
            assertTrue(dao.findByUserId(3L).isEmpty());
        }

        @Test
        void setQuantity_negativeDeletes() {
            dao.addOrUpdateQuantity(4L, 40L, 2);
            boolean ok = dao.setQuantity(4L, 40L, -5);
            assertTrue(ok);
            assertTrue(dao.findByUserId(4L).isEmpty());
        }

        @Test
        void setQuantity_nonExistingReturnsFalse() {
            assertFalse(dao.setQuantity(5L, 50L, 3));
        }

        @Test
        void findByUserId_empty() {
            assertTrue(dao.findByUserId(6L).isEmpty());
        }

        @Test
        void deleteItem_existing() {
            dao.addOrUpdateQuantity(7L, 70L, 3);
            assertTrue(dao.deleteItem(7L, 70L));
            assertTrue(dao.findByUserId(7L).isEmpty());
        }

        @Test
        void deleteItem_nonExisting() {
            assertFalse(dao.deleteItem(8L, 80L));
        }

        @Test
        void deleteAllByUserId_existing() {
            dao.addOrUpdateQuantity(9L, 90L, 1);
            dao.addOrUpdateQuantity(9L, 91L, 2);
            assertTrue(dao.deleteAllByUserId(9L));
            assertTrue(dao.findByUserId(9L).isEmpty());
        }

        @Test
        void deleteAllByUserId_none() {
            assertFalse(dao.deleteAllByUserId(10L));
        }
    }
