package epam.finalProject;

import epam.finalProject.DAO.GenreDaoImpl;
import epam.finalProject.db.ConnectionPool;
import epam.finalProject.entity.Genre;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenreDaoImplTest {

    private DataSource ds;
    private GenreDaoImpl dao;

    @BeforeAll
    void initDatabase() throws Exception {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:genreDb;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        h2.setPassword("");
        ds = h2;

        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                        CREATE TABLE genres (
                          id   BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL
                        );
                    """);
        }

        ConnectionPool.setTestDataSource(ds);
    }

    @BeforeEach
    void setUp() throws Exception {
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE genres");
        }
        dao = new GenreDaoImpl(ds);
    }

    @Test
    void save_ShouldInsertRowAndSetId() {
        Genre g = new Genre();
        g.setName("Fantasy");

        boolean ok = dao.save(g);
        assertTrue(ok, "save() должно вернуть true при успешном insert");
        assertNotNull(g.getId(), "после save() поле id должно быть не-null");
        assertTrue(g.getId() > 0, "id должен быть положительным");
    }

    @Test
    void save_DuplicateName_ShouldStillInsert() {
        Genre g1 = new Genre();
        g1.setName("Horror");
        Genre g2 = new Genre();
        g2.setName("Horror");

        assertTrue(dao.save(g1));
        assertTrue(dao.save(g2), "нет уникального ограничения, второй insert тоже успешен");
        List<Genre> all = dao.findAll();
        assertEquals(2, all.size(), "должно быть два жанра с одним именем");
    }

    @Test
    void findAll_EmptyTable_ShouldReturnEmptyList() {
        List<Genre> list = dao.findAll();
        assertNotNull(list);
        assertTrue(list.isEmpty(), "пустая таблица → пустой список");
    }

    @Test
    void findAll_AfterSaves_ShouldReturnAll() {
        Genre g1 = new Genre();
        g1.setName("Adventure");
        Genre g2 = new Genre();
        g2.setName("Romance");
        dao.save(g1);
        dao.save(g2);

        List<Genre> list = dao.findAll();
        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(g -> "Adventure".equals(g.getName())));
        assertTrue(list.stream().anyMatch(g -> "Romance".equals(g.getName())));
    }

    @Test
    void findById_Existing_ShouldReturnGenre() {
        Genre g = new Genre();
        g.setName("Sci-Fi");
        dao.save(g);
        Long id = g.getId();

        Genre fromDb = dao.findById(id);
        assertNotNull(fromDb);
        assertEquals(id, fromDb.getId());
        assertEquals("Sci-Fi", fromDb.getName());
    }

    @Test
    void findById_NonExisting_ShouldReturnNull() {
        Genre fromDb = dao.findById(999L);
        assertNull(fromDb, "нет жанра с таким id → возвращается null");
    }
}
