package epam.finalProject.DAO;

import epam.finalProject.entity.Genre;
import epam.finalProject.db.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link GenreDao}.
 * Provides CRUD operations for {@link Genre} entities.
 * Uses a provided {@link DataSource} or a default {@link ConnectionPool}.
 */
public class GenreDaoImpl implements GenreDao {

    private static final Logger logger = LoggerFactory.getLogger(GenreDaoImpl.class);

    private final DataSource ds;

    /**
     * Constructs a GenreDaoImpl using the specified DataSource.
     *
     * @param ds the DataSource to obtain connections from
     */
    public GenreDaoImpl(DataSource ds) {
        this.ds = ds;
        logger.debug("GenreDaoImpl initialized with provided DataSource");
    }

    /**
     * Constructs a GenreDaoImpl using the default ConnectionPool.
     */
    public GenreDaoImpl() {
        this.ds = null;
        logger.debug("GenreDaoImpl initialized using default ConnectionPool");
    }

    /**
     * Obtains a database connection, either from the configured DataSource or from ConnectionPool.
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
     * Inserts a new {@link Genre} into the database.
     *
     * @param genre the Genre to save (must have a non-null name)
     * @return {@code true} if the insertion affected at least one row, {@code false} otherwise
     */
    @Override
    public boolean save(Genre genre) {
        logger.debug("save(Genre) called for name='{}'", genre.getName());
        String sql = "INSERT INTO genres (name) VALUES (?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, genre.getName());
            logger.debug("Executing INSERT: {} with name='{}'", sql, genre.getName());
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                logger.warn("No rows inserted for genre name='{}'", genre.getName());
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    genre.setId(keys.getLong(1));
                    logger.debug("Inserted genre with id={} name='{}'", genre.getId(), genre.getName());
                }
            }
            return true;
        } catch (SQLException e) {
            logger.error("SQLException in save(Genre) for name='{}': {}", genre.getName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Retrieves all {@link Genre} records from the database.
     *
     * @return a List of all genres, or an empty list if none found or on error
     */
    @Override
    public List<Genre> findAll() {
        logger.debug("findAll() called to retrieve all genres");
        List<Genre> genres = new ArrayList<>();
        String sql = "SELECT id, name FROM genres";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            logger.debug("Executing SELECT: {}", sql);
            while (rs.next()) {
                Genre genre = new Genre();
                genre.setId(rs.getLong("id"));
                genre.setName(rs.getString("name"));
                genres.add(genre);
            }
            logger.debug("Number of genres retrieved: {}", genres.size());
        } catch (SQLException e) {
            logger.error("SQLException in findAll(): {}", e.getMessage(), e);
        }
        return genres;
    }

    /**
     * Retrieves a {@link Genre} by its ID.
     *
     * @param id the ID of the genre to retrieve
     * @return the Genre if found, or {@code null} if not found or on error
     */
    @Override
    public Genre findById(Long id) {
        logger.debug("findById() called for genre id={}", id);
        String sql = "SELECT id, name FROM genres WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            logger.debug("Executing SELECT: {} with id={}", sql, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("id"));
                    genre.setName(rs.getString("name"));
                    logger.debug("Genre found: id={} name='{}'", genre.getId(), genre.getName());
                    return genre;
                } else {
                    logger.warn("No genre found for id={}", id);
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("SQLException in findById(id) for id={}: {}", id, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<Genre> findByBookId(long bookId) throws SQLException {
        String sql = "SELECT g.id, g.name " + "FROM genres g " + "JOIN book_genre bg ON g.id = bg.genre_id " + "WHERE bg.book_id = ?";
        List<Genre> genres = new ArrayList<>();

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Genre g = new Genre();
                    g.setId(rs.getLong("id"));
                    g.setName(rs.getString("name"));
                    genres.add(g);
                }
            }
        }
        return genres;
    }


}
