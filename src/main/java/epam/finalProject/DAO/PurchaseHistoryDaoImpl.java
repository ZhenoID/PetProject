package epam.finalProject.DAO;

import epam.finalProject.db.ConnectionPool;
import epam.finalProject.entity.PurchaseHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link PurchaseHistoryDao}.
 * Provides methods to save a purchase record and retrieve purchase history by user ID.
 * Uses {@link ConnectionPool} to obtain database connections.
 */
public class PurchaseHistoryDaoImpl implements PurchaseHistoryDao {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseHistoryDaoImpl.class);

    /**
     * Obtains a connection from the {@link ConnectionPool}.
     *
     * @return a new {@link Connection}
     * @throws SQLException if a database access error occurs
     */
    private Connection getConnection() throws SQLException {
        logger.debug("Acquiring connection from ConnectionPool");
        return ConnectionPool.getInstance().getConnection();
    }

    /**
     * Saves a {@link PurchaseHistory} record into the database.
     * After insertion, sets the generated ID on the record object.
     *
     * @param record the PurchaseHistory record to save (must contain userId, bookId, quantity, purchaseDate)
     * @return {@code true} if insertion succeeded, {@code false} otherwise
     */
    @Override
    public boolean save(PurchaseHistory record) {
        String sql = "INSERT INTO purchase_history (user_id, book_id, quantity, purchase_date) VALUES (?, ?, ?, ?)";
        logger.debug("save(PurchaseHistory) called for userId={}, bookId={}, quantity={}", record.getUserId(), record.getBookId(), record.getQuantity());

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, record.getUserId());
            ps.setLong(2, record.getBookId());
            ps.setInt(3, record.getQuantity());
            ps.setTimestamp(4, record.getPurchaseDate());
            logger.debug("Executing INSERT: {} with values userId={}, bookId={}, quantity={}, purchaseDate={}", sql, record.getUserId(), record.getBookId(), record.getQuantity(), record.getPurchaseDate());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                logger.warn("No rows inserted for PurchaseHistory record: {}", record);
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long generatedId = keys.getLong(1);
                    record.setId(generatedId);
                    logger.debug("PurchaseHistory saved with generated ID={}", generatedId);
                }
            }
            return true;

        } catch (SQLException e) {
            logger.error("Database error in save(PurchaseHistory): userId={}, bookId={}, quantity={}", record.getUserId(), record.getBookId(), record.getQuantity(), e);
            return false;
        }
    }

    /**
     * Retrieves all {@link PurchaseHistory} records for a given user, ordered by purchase date descending.
     *
     * @param userId the ID of the user whose purchase history is fetched
     * @return a list of PurchaseHistory records; empty list if none found or on error
     */
    @Override
    public List<PurchaseHistory> findByUserId(Long userId) {
        String sql = "SELECT id, user_id, book_id, quantity, purchase_date " + "FROM purchase_history WHERE user_id = ? ORDER BY purchase_date DESC";
        logger.debug("findByUserId() called for userId={}", userId);

        List<PurchaseHistory> result = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            logger.debug("Executing SELECT: {} with userId={}", sql, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseHistory ph = new PurchaseHistory();
                    ph.setId(rs.getLong("id"));
                    ph.setUserId(rs.getLong("user_id"));
                    ph.setBookId(rs.getLong("book_id"));
                    ph.setQuantity(rs.getInt("quantity"));

                    Timestamp ts = rs.getTimestamp("purchase_date");
                    ph.setPurchaseDate(ts);

                    result.add(ph);
                }
                logger.debug("Fetched {} purchase history records for userId={}", result.size(), userId);
            }

        } catch (SQLException e) {
            logger.error("Database error in findByUserId(): userId={}", userId, e);
        }
        return result;
    }
}
