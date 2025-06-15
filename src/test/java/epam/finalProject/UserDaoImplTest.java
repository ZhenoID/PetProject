package epam.finalProject;

import epam.finalProject.DAO.UserDaoImpl;
import epam.finalProject.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDaoImplTest {

    private DataSource ds;
    private UserDaoImpl dao;

    @BeforeAll
    void initDatabase() throws Exception {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:userDb;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        h2.setPassword("");
        ds = h2;

        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                        CREATE TABLE users (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          username VARCHAR(255) UNIQUE NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          role VARCHAR(50) NOT NULL
                        );
                    """);
        }
    }

    @BeforeEach
    void setUp() {
        dao = new UserDaoImpl(ds);
    }

    @AfterEach
    void cleanUp() throws Exception {
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE users");
        }
    }

    @Test
    void save_and_findByUsername() {
        User u = new User();
        u.setUsername("alice");
        u.setPassword("secret");
        u.setRole("ROLE_ADMIN");

        assertTrue(dao.save(u), "save() должен вернуть true для нового пользователя");

        User fromDb = dao.findByUsername("alice");
        assertNotNull(fromDb, "findByUsername должен вернуть объект");
        assertEquals("alice", fromDb.getUsername());
        assertEquals("secret", fromDb.getPassword());
        assertEquals("ROLE_ADMIN", fromDb.getRole());
    }

    @Test
    void findByUsername_nonExisting_returnsNull() {
        assertNull(dao.findByUsername("no_such_user"));
    }


}
