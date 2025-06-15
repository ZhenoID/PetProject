package epam.finalProject.DAO;

import epam.finalProject.db.ConnectionPool;
import epam.finalProject.entity.Book;
import epam.finalProject.entity.Author;
import epam.finalProject.entity.Genre;
import epam.finalProject.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link BookDao}.
 * Provides CRUD operations for {@link Book} entities, including associated author and genre handling.
 * Utilizes a {@link DataSource} if provided, or a default {@link ConnectionPool} otherwise.
 */
public class BookDaoImpl implements BookDao {

    private static final Logger logger = LoggerFactory.getLogger(BookDaoImpl.class);

    private static final String FIND_AUTHOR_SQL = "SELECT id FROM authors WHERE name = ?";
    private static final String INSERT_AUTHOR_SQL = "INSERT INTO authors (name) VALUES (?)";
    private static final String INSERT_BOOK_SQL = "INSERT INTO books (title, year, author_id, description, quantity) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_BOOK_GENRE_SQL = "INSERT INTO book_genres (book_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_BOOK_SQL = "DELETE FROM books WHERE id = ?";
    private static final String UPDATE_BOOK_SQL = "UPDATE books SET title = ?, author_id = ?, year = ?, description = ?, quantity = ? WHERE id = ?";
    private static final String SELECT_ALL_BOOKS_SQL = "SELECT * FROM books ORDER BY id";
    private static final String SELECT_BOOK_BY_ID_SQL = "SELECT * FROM books WHERE id = ?";
    private static final String SELECT_GENRES_FOR_BOOK_SQL = "SELECT g.id, g.name FROM genres g JOIN book_genres bg ON g.id = bg.genre_id WHERE bg.book_id = ?";

    private final DataSource ds;

    /**
     * Constructs a BookDaoImpl that uses the provided DataSource.
     *
     * @param ds the DataSource to obtain connections from
     */
    public BookDaoImpl(DataSource ds) {
        this.ds = ds;
        logger.debug("BookDaoImpl initialized with provided DataSource");
    }

    /**
     * Constructs a BookDaoImpl that uses the default ConnectionPool.
     */
    public BookDaoImpl() {
        this.ds = null;
        logger.debug("BookDaoImpl initialized using default ConnectionPool");
    }

    /**
     * Obtains a Connection, either from the configured DataSource or from ConnectionPool.
     *
     * @return a new {@link Connection}
     * @throws SQLException if unable to obtain a connection
     */
    private Connection getConnection() throws SQLException {
        if (ds != null) {
            logger.debug("Acquiring connection from DataSource");
            return ds.getConnection();
        } else {
            logger.debug("Acquiring connection from ConnectionPool");
            return ConnectionPool.getInstance().getConnection();
        }
    }

    /**
     * Deletes the given {@link Book} from the database.
     *
     * @param book the Book to delete (must have a valid ID)
     * @return {@code true} if at least one row was deleted, {@code false} otherwise
     */
    @Override
    public boolean deleteBook(Book book) {
        logger.debug("deleteBook() called for book id={}", book.getId());
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(DELETE_BOOK_SQL)) {
            ps.setLong(1, book.getId());
            logger.debug("Executing DELETE: {} with id={}", DELETE_BOOK_SQL, book.getId());
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                logger.debug("Book deleted successfully: id={}", book.getId());
            } else {
                logger.warn("No book found to delete for id={}", book.getId());
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Error deleting book id={}", book.getId(), e);
            return false;
        }
    }

    /**
     * Updates the database record for the given {@link Book}.
     *
     * @param book the Book containing updated fields and a valid ID
     * @return {@code true} if the update succeeded, {@code false} otherwise
     */
    @Override
    public boolean changeBook(Book book) {
        logger.debug("changeBook() called for book id={}", book.getId());
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(UPDATE_BOOK_SQL)) {
            ps.setString(1, book.getTitle());
            ps.setLong(2, book.getAuthorId());
            ps.setInt(3, book.getYear());
            ps.setString(4, book.getDescription());
            ps.setInt(5, book.getQuantity());
            ps.setLong(6, book.getId());
            logger.debug("Executing UPDATE: {} with values title={}, authorId={}, year={}, description={}, quantity={}, id={}", UPDATE_BOOK_SQL, book.getTitle(), book.getAuthorId(), book.getYear(), book.getDescription(), book.getQuantity(), book.getId());
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.debug("Book updated successfully: id={}", book.getId());
            } else {
                logger.warn("No book found to update for id={}", book.getId());
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Error updating book id={}", book.getId(), e);
            return false;
        }
    }


    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection conn = ConnectionPool.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            logger.error("Error count books", e);
            return 0;
        }
    }

    /**
     * Retrieves all {@link Book} records from the database, including their associated
     * {@link Author} and list of {@link Genre}.
     *
     * @return a List of all Book entities; empty if none found or on error
     */
    @Override
    public List<Book> findAll(Pageable pageable) {
        String sql = """
                SELECT id, title, author_id, year, description, quantity
                  FROM books
                 ORDER BY id
                 LIMIT ? OFFSET ?
                """;
        List<Book> list = new ArrayList<>();

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pageable.getPageSize());
            ps.setLong(2, pageable.getOffset());
            try (ResultSet rs = ps.executeQuery()) {
                AuthorDao authorDao = new AuthorDaoImpl(ds);
                while (rs.next()) {
                    Book b = mapBasicBook(rs);
                    b.setAuthor(authorDao.findById(b.getAuthorId()));
                    b.setGenres(loadGenres(conn, b.getId()));

                    list.add(b);
                }
                return list;
            }

        } catch (SQLException e) {
            logger.error("Error with findAll(Pageable pageable) ", e);
            return list;
        }
    }

    /**
     * Retrieves a {@link Book} by its ID, including its {@link Author} and list of {@link Genre}.
     *
     * @param id the ID of the book to retrieve
     * @return the Book if found, or {@code null} if not found or on error
     */
    @Override
    public Book findById(Long id) {
        logger.debug("findById() called for book id={}", id);
        Book book = null;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(SELECT_BOOK_BY_ID_SQL)) {
            ps.setLong(1, id);
            logger.debug("Executing SELECT: {} with id={}", SELECT_BOOK_BY_ID_SQL, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    book = mapBasicBook(rs);
                    Author author = new AuthorDaoImpl(ds).findById(book.getAuthorId());
                    book.setAuthor(author);
                    book.setGenres(loadGenres(conn, id));
                    logger.debug("Book found: id={} title='{}'", book.getId(), book.getTitle());
                    return book;
                } else {
                    logger.warn("No book found for id={}", id);
                    throw new ResourceNotFoundException("Book with id=" + id + " is not found");
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching book by id={}", id, e);
            return book;
        }
    }

    /**
     * Saves a new {@link Book} along with its {@link Author} and associated {@link Genre} IDs in a single transaction.
     * If the author does not exist, it will be created. The method will commit the transaction if all steps succeed.
     *
     * @param book   the Book entity to save (without ID)
     * @param author the Author entity (may or may not already exist)
     * @return {@code true} if the book and related entities were saved successfully, {@code false} otherwise
     */
    @Override
    public boolean saveBookWithAuthor(Book book, Author author) {
        logger.debug("saveBookWithAuthor() called for book title='{}' author='{}'", book.getTitle(), author.getName());
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            long authorId = getOrCreateAuthor(conn, author);
            long bookId = insertBook(conn, book, authorId);
            book.setId(bookId);

            insertBookGenres(conn, bookId, book.getGenreIds());
            conn.commit();

            logger.debug("Book saved successfully with id={} and authorId={}", bookId, authorId);
            return true;
        } catch (SQLException e) {
            logger.error("Error saving book with author. Rolling back transaction for book title='{}'", book.getTitle(), e);
            return false;
        }
    }

    /**
     * Decrements the quantity of a book in stock, ensuring that the quantity does not go negative.
     *
     * @param bookId the ID of the book to decrement
     * @param amount the amount to subtract from the current quantity
     * @return {@code true} if the quantity was decremented, {@code false} otherwise
     */
    @Override
    public boolean decrementQuantity(Long bookId, int amount) {
        logger.debug("decrementQuantity() called for bookId={} amount={}", bookId, amount);
        String sql = "UPDATE books SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, amount);
            ps.setLong(2, bookId);
            ps.setInt(3, amount);
            logger.debug("Executing UPDATE: {} with amount={}, bookId={}", sql, amount, bookId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.debug("Quantity decremented by {} for bookId={}", amount, bookId);
            } else {
                logger.warn("Failed to decrement quantity for bookId={}. Not enough stock.", bookId);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Error decrementing quantity for bookId={}", bookId, e);
            return false;
        }
    }

    /**
     * Retrieves the ID of an existing author by name, or inserts a new author if none exists.
     *
     * @param conn   the active {@link Connection} in which to execute
     * @param author the Author entity containing the name
     * @return the ID of the existing or newly created author
     * @throws SQLException if any SQL error occurs
     */
    private long getOrCreateAuthor(Connection conn, Author author) throws SQLException {
        logger.debug("getOrCreateAuthor() called for author='{}'", author.getName());
        try (PreparedStatement ps = conn.prepareStatement(FIND_AUTHOR_SQL)) {
            ps.setString(1, author.getName());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long existingId = rs.getLong("id");
                    logger.debug("Author found with id={} for name='{}'", existingId, author.getName());
                    return existingId;
                }
            }
        } catch (SQLException e) {
            logger.error("Error with geting or creating an author", e);
            return 0;
        }
        try (PreparedStatement ps = conn.prepareStatement(INSERT_AUTHOR_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, author.getName());
            logger.debug("Executing INSERT: {} with name='{}'", INSERT_AUTHOR_SQL, author.getName());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long newId = rs.getLong(1);
                    logger.debug("New author created with id={} name='{}'", newId, author.getName());
                    return newId;
                }
            }
        } catch (SQLException e) {
            logger.error("Error with geting or creating an author", e);
            return 0;
        }
        return 0;
    }

    /**
     * Inserts a new book record into the database.
     *
     * @param conn     the active {@link Connection}
     * @param book     the Book to insert (must contain title, year, description, quantity)
     * @param authorId the ID of the existing or newly created author
     * @return the generated ID of the newly inserted book
     * @throws SQLException if any SQL error occurs
     */
    private long insertBook(Connection conn, Book book, long authorId) throws SQLException {
        logger.debug("insertBook() called for book title='{}' authorId={}", book.getTitle(), authorId);
        try (PreparedStatement ps = conn.prepareStatement(INSERT_BOOK_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getTitle());
            ps.setInt(2, book.getYear());
            ps.setLong(3, authorId);
            ps.setString(4, book.getDescription());
            ps.setInt(5, book.getQuantity());
            logger.debug("Executing INSERT: {} with title='{}', year={}, authorId={}, description='{}', quantity={}", INSERT_BOOK_SQL, book.getTitle(), book.getYear(), authorId, book.getDescription(), book.getQuantity());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long newBookId = rs.getLong(1);
                    logger.debug("New book inserted with id={} title='{}'", newBookId, book.getTitle());
                    return newBookId;
                }
            }
        } catch (SQLException e) {
            logger.error("Error with inserting a book", e);
            return 0;
        }
        return 0;
    }

    /**
     * Inserts entries into the book_genres join table for the given book and genre IDs.
     *
     * @param conn     the active {@link Connection}
     * @param bookId   the ID of the book
     * @param genreIds the list of genre IDs to associate with the book
     * @throws SQLException if any SQL error occurs
     */
    private void insertBookGenres(Connection conn, long bookId, List<Long> genreIds) throws SQLException {
        if (genreIds == null || genreIds.isEmpty()) {
            logger.debug("No genres to insert for bookId={}", bookId);
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(INSERT_BOOK_GENRE_SQL)) {
            for (Long gid : genreIds) {
                ps.setLong(1, bookId);
                ps.setLong(2, gid);
                ps.addBatch();
                logger.debug("Adding to batch: bookId={}, genreId={}", bookId, gid);
            }
            ps.executeBatch();
            logger.debug("Inserted {} genre associations for bookId={}", genreIds.size(), bookId);
        }
    }

    /**
     * Loads the list of {@link Genre} entities associated with a given book ID.
     *
     * @param conn   the active {@link Connection}
     * @param bookId the ID of the book for which to load genres
     * @return a List of Genre entities; empty if none found
     * @throws SQLException if any SQL error occurs
     */
    private List<Genre> loadGenres(Connection conn, long bookId) throws SQLException {
        logger.debug("loadGenres() called for bookId={}", bookId);
        List<Genre> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_GENRES_FOR_BOOK_SQL)) {
            ps.setLong(1, bookId);
            logger.debug("Executing SELECT: {} with bookId={}", SELECT_GENRES_FOR_BOOK_SQL, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Genre g = new Genre();
                    g.setId(rs.getLong("id"));
                    g.setName(rs.getString("name"));
                    list.add(g);
                }
            }
        }
        logger.debug("Loaded {} genres for bookId={}", list.size(), bookId);
        return list;
    }

    /**
     * Maps the current row of a ResultSet to a basic {@link Book} object without author or genres.
     *
     * @param rs the ResultSet positioned at a valid row
     * @return a Book object populated with fields from the ResultSet
     * @throws SQLException if any SQL error occurs
     */
    private Book mapBasicBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthorId(rs.getLong("author_id"));
        book.setYear(rs.getInt("year"));
        book.setDescription(rs.getString("description"));
        book.setQuantity(rs.getInt("quantity"));
        logger.debug("Mapped basic book: id={}, title='{}'", book.getId(), book.getTitle());
        return book;
    }
}
