//package epam.finalProject;
//
//import epam.finalProject.DAO.BookDaoImpl;
//import epam.finalProject.db.ConnectionPool;
//import epam.finalProject.entity.Author;
//import epam.finalProject.entity.Book;
//import epam.finalProject.entity.Genre;
//import epam.finalProject.exception.ResourceNotFoundException;
//import org.h2.tools.RunScript;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
//
//import javax.sql.DataSource;
//import java.io.StringReader;
//import java.sql.Connection;
//import java.sql.Statement;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class BookDaoImplTest {
//
//    private BookDaoImpl dao;
//    private DataSource ds;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        ds = new DriverManagerDataSource(
//                "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
//                "sa", ""
//        );
//
//        try (Connection conn = ds.getConnection()) {
//            conn.createStatement().execute("DROP ALL OBJECTS");
//            String schema = """
//                CREATE TABLE authors (
//                  id   SERIAL PRIMARY KEY,
//                  name VARCHAR(100) NOT NULL
//                );
//                CREATE TABLE genres (
//                  id   SERIAL PRIMARY KEY,
//                  name VARCHAR(100) NOT NULL
//                );
//                CREATE TABLE books (
//                  id          SERIAL PRIMARY KEY,
//                  title       VARCHAR(200) NOT NULL,
//                  author_id   INT REFERENCES authors(id),
//                  \"year\"        INT,
//                  description TEXT,
//                  quantity    INT NOT NULL DEFAULT 0
//                );
//                CREATE TABLE book_genres (
//                  book_id  INT NOT NULL REFERENCES books(id),
//                  genre_id INT NOT NULL REFERENCES genres(id),
//                  PRIMARY KEY (book_id, genre_id)
//                );
//                """;
//            RunScript.execute(conn, new StringReader(schema));
//
//            String data = """
//                INSERT INTO authors(name) VALUES('Author One');
//                INSERT INTO genres(name)  VALUES('Genre A');
//                INSERT INTO genres(name)  VALUES('Genre B');
//                INSERT INTO books(title,\"year\",author_id,description,quantity)
//                    VALUES('Book One',2001,1,'Desc One',10);
//                INSERT INTO books(title,\"year\",author_id,description,quantity)
//                    VALUES('Book Two',2002,1,'Desc Two',5);
//                INSERT INTO book_genres(book_id,genre_id) VALUES(1,1);
//                INSERT INTO book_genres(book_id,genre_id) VALUES(1,2);
//                """;
//            RunScript.execute(conn, new StringReader(data));
//        }
//
//        ConnectionPool.setTestDataSource(ds);
//
//        dao = new BookDaoImpl(ds);
//    }
//
//    @Test
//    void count_shouldReturnNumberOfBooks() {
//        assertEquals(2, dao.count());
//    }
//
//    @Test
//    void findById_existing_shouldReturnFullBook() {
//        Book b = dao.findById(1L);
//        assertEquals(1L, b.getId());
//        assertEquals("Book One", b.getTitle());
//        assertEquals(10, b.getQuantity());
//        assertNotNull(b.getAuthor());
//        assertEquals("Author One", b.getAuthor().getName());
//        List<Genre> genres = b.getGenres();
//        assertEquals(2, genres.size());
//        assertTrue(genres.stream().anyMatch(g -> "Genre A".equals(g.getName())));
//        assertTrue(genres.stream().anyMatch(g -> "Genre B".equals(g.getName())));
//    }
//
//    @Test
//    void findById_missing_shouldThrow() {
//        assertThrows(ResourceNotFoundException.class, () -> dao.findById(999L));
//    }
//
//
//    @Test
//    void saveBookWithAuthor_newAuthorAndBook_shouldPersist() {
//        Book newBook = new Book();
//        newBook.setTitle("New Title");
//        newBook.setYear(2025);
//        newBook.setDescription("New Desc");
//        newBook.setQuantity(7);
//        newBook.setGenreIds(List.of());
//
//        Author newAuthor = new Author();
//        newAuthor.setName("Fresh Author");
//
//        boolean ok = dao.saveBookWithAuthor(newBook, newAuthor);
//        assertTrue(ok);
//        assertTrue(newBook.getId() > 0);
//
//        assertEquals(3, dao.count());
//
//        Book persisted = dao.findById(newBook.getId());
//        assertEquals("New Title", persisted.getTitle());
//        assertEquals("Fresh Author", persisted.getAuthor().getName());
//        assertTrue(persisted.getGenres().isEmpty());
//    }
//
//    @Test
//    void changeBook_existing_shouldUpdateFields() {
//        Book b = dao.findById(2L);
//        b.setTitle("Updated");
//        b.setQuantity(42);
//        boolean ok = dao.changeBook(b);
//        assertTrue(ok);
//
//        Book reloaded = dao.findById(2L);
//        assertEquals("Updated", reloaded.getTitle());
//        assertEquals(42, reloaded.getQuantity());
//    }
//
//    @Test
//    void deleteBook_existing_shouldRemove() {
//        Book b = dao.findById(2L);
//        boolean ok = dao.deleteBook(b);
//        assertTrue(ok);
//        assertEquals(1, dao.count());
//        assertThrows(ResourceNotFoundException.class, () -> dao.findById(2L));
//    }
//
//    @Test
//    void decrementQuantity_enoughStock_shouldReturnTrue() {
//        assertTrue(dao.decrementQuantity(1L, 3));
//        Book b = dao.findById(1L);
//        assertEquals(7, b.getQuantity());
//    }
//
//    @Test
//    void decrementQuantity_insufficientStock_shouldReturnFalse() {
//        assertFalse(dao.decrementQuantity(1L, 999));
//        Book b = dao.findById(1L);
//        assertEquals(10, b.getQuantity());
//    }
//}
