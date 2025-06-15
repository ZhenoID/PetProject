package epam.finalProject.DAO;

import epam.finalProject.db.ConnectionPool;
import epam.finalProject.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import org.springframework.data.domain.Pageable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;

/**
 * JDBC implementation of {@link UserDao}.
 * Provides CRUD operations for {@link User} entities, including saving new users,
 * finding by username or ID, updating password and role, deleting users, and listing all users.
 * Uses a provided {@link DataSource} or a default {@link ConnectionPool}.
 */
public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    private final DataSource ds;

    /**
     * Constructs a UserDaoImpl using the specified DataSource (for testing).
     *
     * @param ds the DataSource to obtain connections from
     */
    public UserDaoImpl(DataSource ds) {
        this.ds = ds;
        logger.debug("UserDaoImpl initialized with provided DataSource");
    }

    /**
     * Constructs a UserDaoImpl using the default ConnectionPool (for production).
     */
    public UserDaoImpl() {
        this.ds = null;
        logger.debug("UserDaoImpl initialized using default ConnectionPool");
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
     * Inserts a new {@link User} into the database.
     *
     * @param user the User to save (must contain username, password, and role)
     * @return {@code true} if the insertion succeeded, {@code false} otherwise
     */
    @Override
    public boolean save(User user) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        logger.debug("save(User) called for username='{}'", user.getUsername());
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            logger.debug("Executing INSERT: {} with username='{}', role='{}'", sql, user.getUsername(), user.getRole());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                logger.warn("No rows inserted for User: username='{}'", user.getUsername());
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long newId = keys.getLong(1);
                    user.setId(newId);
                    logger.debug("User saved with generated ID={} username='{}'", newId, user.getUsername());
                }
            }
            return true;
        } catch (SQLException e) {
            logger.error("Database error in save() for username='{}': {}", user.getUsername(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Finds a {@link User} by username.
     *
     * @param username the username to search for
     * @return the User if found, or {@code null} if not found or on error
     */
    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        logger.debug("findByUsername() called for username='{}'", username);
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            logger.debug("Executing SELECT: {} with username='{}'", sql, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(rs.getString("role"));
                    user.setPassword(rs.getString("password"));
                    logger.debug("User found: id={}, username='{}', role='{}'", user.getId(), user.getUsername(), user.getRole());
                    return user;
                } else {
                    logger.warn("No user found for username='{}'", username);
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in findByUsername() for username='{}': {}", username, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Updates the password of an existing {@link User}.
     *
     * @param user the User containing the username and new hashed password
     * @return {@code true} if the update succeeded, {@code false} otherwise
     */
    @Override
    public boolean updatePassword(User user) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        logger.debug("updatePassword() called for username='{}'", user.getUsername());
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getPassword());
            ps.setString(2, user.getUsername());
            logger.debug("Executing UPDATE: {} with username='{}'", sql, user.getUsername());

            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.debug("Password updated successfully for username='{}'", user.getUsername());
            } else {
                logger.warn("No user found to update password for username='{}'", user.getUsername());
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Database error in updatePassword() for username='{}': {}", user.getUsername(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Retrieves all {@link User} records from the database.
     *
     * @return a List of all users; empty list if none found or on error
     */
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = ConnectionPool.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> findAll(Pageable pageable) {
        String sql = "SELECT id, username, password, role " + "FROM users ORDER BY id " + "LIMIT ? OFFSET ?";
        try (Connection conn = ConnectionPool.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pageable.getPageSize());
            ps.setLong(2, pageable.getOffset());
            try (ResultSet rs = ps.executeQuery()) {
                List<User> result = new ArrayList<>();
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getLong("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPassword(rs.getString("password"));
                    u.setRole(rs.getString("role"));
                    result.add(u);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the specified {@link User} from the database.
     *
     * @param user the User to delete (must contain a valid ID)
     * @return {@code true} if the deletion succeeded, {@code false} otherwise
     */
    @Override
    public boolean delete(User user) {
        String sql = "DELETE FROM users WHERE id = ?";
        logger.debug("delete(User) called for id={}", user.getId());
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, user.getId());
            logger.debug("Executing DELETE: {} with id={}", sql, user.getId());
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                logger.debug("User deleted successfully: id={}", user.getId());
            } else {
                logger.warn("No user found to delete for id={}", user.getId());
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Database error in delete() for id={}: {}", user.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Finds a {@link User} by its ID.
     *
     * @param id the ID of the user to retrieve
     * @return the User if found, or {@code null} if not found or on error
     */
    @Override
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        logger.debug("findById() called for id={}", id);
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            logger.debug("Executing SELECT: {} with id={}", sql, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(rs.getString("role"));
                    user.setPassword(rs.getString("password"));
                    logger.debug("User found: id={}, username='{}', role='{}'", user.getId(), user.getUsername(), user.getRole());
                    return user;
                } else {
                    logger.warn("No user found for id={}", id);
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Database error in findById() for id={}: {}", id, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Updates the role of an existing {@link User}.
     *
     * @param id      the ID of the user to update
     * @param newRole the new role to assign
     * @return {@code true} if the update succeeded, {@code false} otherwise
     */
    @Override
    public boolean updateRole(Long id, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        logger.debug("updateRole() called for id={} newRole='{}'", id, newRole);
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newRole);
            ps.setLong(2, id);
            logger.debug("Executing UPDATE: {} with newRole='{}', id={}", sql, newRole, id);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.debug("User role updated successfully: id={} newRole='{}'", id, newRole);
            } else {
                logger.warn("No user found to update role for id={}", id);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Database error in updateRole() for id={} newRole='{}': {}", id, newRole, e.getMessage(), e);
            return false;
        }
    }
}
