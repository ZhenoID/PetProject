package epam.finalProject;

import epam.finalProject.DAO.AuthorDaoImpl;
import epam.finalProject.entity.Author;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AuthorDaoImplTest {

    private AuthorDaoImpl dao;

    @BeforeEach
    void setUp() throws Exception {
        var ds = new DriverManagerDataSource("jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        try (Connection conn = ds.getConnection()) {
            conn.createStatement().execute("DROP ALL OBJECTS");
            RunScript.execute(conn, new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("schema-h2.sql")));
            RunScript.execute(conn, new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("data.sql")));
        }
        dao = new AuthorDaoImpl(ds);
    }

    @Test
    void findAll_shouldReturnAuthors() {
        List<Author> list = dao.findAll();
        assertFalse(list.isEmpty());
    }

    @Test
    void findById_existing_shouldReturnAuthor() {
        Author a = dao.findById(1L);
        assertNotNull(a);
        assertEquals(1L, a.getId());
    }

    @Test
    void findById_missing_shouldReturnNull() {
        assertNull(dao.findById(999L));
    }

    @ParameterizedTest
    @CsvSource({
            "1, true",
            "999, false"
    })
    void existsById_shouldReturnExpected(long id, boolean expected) {
        assertEquals(expected, dao.existsById(id));
    }

    @Test
    void save_existingName_shouldReturnFalseAndSetId() {
        Author a = new Author();
        a.setName("Existing Author");
        assertFalse(dao.save(a));
        assertEquals(1L, a.getId());
    }

    @Test
    void save_newAuthor_shouldReturnTrueAndGenerateId() {
        Author a = new Author();
        a.setName("Brand New Author");
        assertTrue(dao.save(a));
        assertTrue(a.getId() > 1);
    }

    @Test
    void update_existingAuthor_shouldReturnTrue() {
        Author a = new Author();
        a.setId(1L);
        a.setName("Updated Name");
        assertTrue(dao.update(a));
        assertEquals("Updated Name", dao.findById(1L).getName());
    }

    @Test
    void update_missingAuthor_shouldReturnFalse() {
        Author a = new Author();
        a.setId(999L);
        a.setName("No One");
        assertFalse(dao.update(a));
    }

    @Test
    void delete_existingAuthor_shouldReturnTrueAndThenNotExist() {
        assertTrue(dao.delete(1L));
        assertFalse(dao.existsById(1L));
    }

    @Test
    void delete_missingAuthor_shouldReturnFalse() {
        assertFalse(dao.delete(999L));
    }
}
