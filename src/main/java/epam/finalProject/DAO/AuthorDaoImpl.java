package epam.finalProject.DAO;

import epam.finalProject.db.ConnectionPool;
import epam.finalProject.entity.Author;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link AuthorDao}.
 * Provides CRUD operations for the {@link Author} entity using a configurable {@link DataSource}
 * or a default {@link ConnectionPool}.
 */
public class AuthorDaoImpl implements AuthorDao {

    private static final Logger logger = LoggerFactory.getLogger(AuthorDaoImpl.class);

    private final DataSource ds;

    /**
     * Constructs an AuthorDaoImpl using the given DataSource.
     *
     * @param ds the DataSource to obtain connections from
     */
    public AuthorDaoImpl(DataSource ds) {
        this.ds = ds;
        logger.debug("AuthorDaoImpl initialized with provided DataSource");
    }

    /**
     * Constructs an AuthorDaoImpl using the default ConnectionPool.
     */
    public AuthorDaoImpl() {
        this.ds = null;
        logger.debug("AuthorDaoImpl initialized using default ConnectionPool");
    }

    /**
     * Obtains a new database connection, either from the configured DataSource or from ConnectionPool.
     *
     * @return a new {@link Connection}
     * @throws SQLException if a database access error occurs
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
     * Inserts a new {@link Author} into the database if an author with the same name does not exist.
     * If an author with that name already exists, sets the existing ID on the passed author and returns false.
     *
     * @param author the Author to save (must have a non-null name)
     * @return true if a new row was inserted, false if the author already existed
     */
    @Override
    public boolean save(Author author) {
        logger.debug("save(Author) called for author name='{}'", author.getName());
        String checkSql = "SELECT id FROM authors WHERE name = ?";
        String insertSql = "INSERT INTO authors (name) VALUES (?)";

        try (Connection conn = getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, author.getName());
                logger.debug("Executing check query: {} with name='{}'", checkSql, author.getName());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    long existingId = rs.getLong("id");
                    author.setId(existingId);
                    logger.debug("Author already exists with id={} and name='{}'", existingId, author.getName());
                    return false;
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, author.getName());
                logger.debug("Executing insert: {} with name='{}'", insertSql, author.getName());
                int affectedRows = insertStmt.executeUpdate();

                if (affectedRows == 0) {
                    logger.warn("Insert affected 0 rows for author name='{}'", author.getName());
                    return false;
                }

                try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        long generatedId = keys.getLong(1);
                        author.setId(generatedId);
                        logger.debug("Inserted new author with id={} and name='{}'", generatedId, author.getName());
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            logger.error("SQLException in save(Author) for name='{}': {}", author.getName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Updates the name of an existing {@link Author} in the database.
     *
     * @param author the Author with updated name and a valid existing ID
     * @return true if the update affected at least one row, false otherwise
     */
    @Override
    public boolean update(Author author) {
        logger.debug("update(Author) called for id={} name='{}'", author.getId(), author.getName());
        String sql = "UPDATE authors SET name = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, author.getName());
            ps.setLong(2, author.getId());
            logger.debug("Executing update: {} with name='{}', id={}", sql, author.getName(), author.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.debug("Updated author id={} successfully", author.getId());
                return true;
            } else {
                logger.warn("No author updated for id={}", author.getId());
                return false;
            }

        } catch (SQLException e) {
            logger.error("SQLException in update(Author) for id='{}': {}", author.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Deletes the {@link Author} with the specified ID from the database.
     *
     * @param id the ID of the author to delete
     * @return true if deletion affected at least one row, false otherwise
     */
    @Override
    public boolean delete(Long id) {
        logger.debug("delete(id) called for author id={}", id);
        String sql = "DELETE FROM authors WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            logger.debug("Executing delete: {} with id={}", sql, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.debug("Deleted author id={} successfully", id);
                return true;
            } else {
                logger.warn("No author found to delete for id={}", id);
                return false;
            }

        } catch (SQLException e) {
            logger.error("SQLException in delete(id) for id='{}': {}", id, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Finds the {@link Author} with the given ID.
     *
     * @param id the ID of the author to retrieve
     * @return the Author object if found, or null if not found or on error
     */
    @Override
    public Author findById(Long id) {
        logger.debug("findById(id) called for author id={}", id);
        String sql = "SELECT * FROM authors WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            logger.debug("Executing query: {} with id={}", sql, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Author author = new Author();
                author.setId(rs.getLong("id"));
                author.setName(rs.getString("name"));
                logger.debug("Author found: id={} name='{}'", author.getId(), author.getName());
                return author;
            } else {
                logger.warn("No author found for id={}", id);
                return null;
            }

        } catch (SQLException e) {
            logger.error("SQLException in findById(id) for id='{}': {}", id, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Retrieves all {@link Author} records from the database.
     *
     * @return a List of all authors, or an empty list if none found or on error
     */
    @Override
    public List<Author> findAll() {
        logger.debug("findAll() called to retrieve all authors");
        List<Author> authors = new ArrayList<>();
        String sql = "SELECT * FROM authors";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            logger.debug("Executing query: {}", sql);
            while (rs.next()) {
                Author author = new Author();
                author.setId(rs.getLong("id"));
                author.setName(rs.getString("name"));
                authors.add(author);
            }
            logger.debug("Number of authors retrieved: {}", authors.size());
        } catch (SQLException e) {
            logger.error("SQLException in findAll(): {}", e.getMessage(), e);
        }
        return authors;
    }

    /**
     * Checks whether an {@link Author} with the given ID exists in the database.
     *
     * @param id the ID to check for existence
     * @return true if an author with that ID exists, false otherwise
     */
    @Override
    public boolean existsById(Long id) {
        logger.debug("existsById(id) called for author id={}", id);
        String sql = "SELECT 1 FROM authors WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            logger.debug("Executing query: {} with id={}", sql, id);
            ResultSet rs = ps.executeQuery();
            boolean exists = rs.next();
            logger.debug("Author id={} exists: {}", id, exists);
            return exists;

        } catch (SQLException e) {
            logger.error("SQLException in existsById(id) for id='{}': {}", id, e.getMessage(), e);
            return false;
        }
    }
}
