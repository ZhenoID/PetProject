package epam.finalProject.DAO;

import epam.finalProject.db.ConnectionPool;
import epam.finalProject.entity.BasketItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link BasketDao}.
 * Provides operations to add, update, remove, and retrieve basket items for a user.
 * Utilizes a {@link ConnectionPool} to obtain database connections.
 */
public class BasketDaoImpl implements BasketDao {
    private static final Logger logger = LoggerFactory.getLogger(BasketDaoImpl.class);

    /**
     * Obtains a database connection from the connection pool.
     *
     * @return a new {@link Connection} from {@link ConnectionPool}
     * @throws SQLException if a database access error occurs
     */
    private Connection getConnection() throws SQLException {
        return ConnectionPool.getInstance().getConnection();
    }

    /**
     * Adds a new item to the basket or updates the quantity of an existing item.
     * If the update causes quantity to become zero or negative, the item is removed.
     *
     * @param userId the ID of the user whose basket is being modified
     * @param bookId the ID of the book to add or update in the basket
     * @param delta  the amount to change the quantity by (positive to increase, negative to decrease)
     * @return {@code true} if the operation succeeded, {@code false} otherwise
     */
    @Override
    public boolean addOrUpdateQuantity(Long userId, Long bookId, int delta) {
        String updateSql = "UPDATE basket_items SET quantity = quantity + ? " + "WHERE user_id = ? AND book_id = ?";
        String insertSql = "INSERT INTO basket_items (user_id, book_id, quantity) VALUES (?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setInt(1, delta);
                psUpdate.setLong(2, userId);
                psUpdate.setLong(3, bookId);
                logger.debug("Executing UPDATE for basket_items: userId={}, bookId={}, delta={}", userId, bookId, delta);
                int updated = psUpdate.executeUpdate();
                if (updated > 0) {
                    String deleteIfZero = "DELETE FROM basket_items WHERE user_id = ? AND book_id = ? AND quantity <= 0";
                    try (PreparedStatement psDel = conn.prepareStatement(deleteIfZero)) {
                        psDel.setLong(1, userId);
                        psDel.setLong(2, bookId);
                        logger.debug("Executing DELETE IF ZERO for basket_items: userId={}, bookId={}", userId, bookId);
                        psDel.executeUpdate();
                    }
                    conn.commit();
                    logger.debug("Quantity updated successfully for userId={}, bookId={} (delta={})", userId, bookId, delta);
                    return true;
                }
            }

            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setLong(1, userId);
                psInsert.setLong(2, bookId);
                psInsert.setInt(3, delta);
                logger.debug("Executing INSERT into basket_items: userId={}, bookId={}, quantity={}", userId, bookId, delta);
                psInsert.executeUpdate();
            }

            conn.commit();
            logger.debug("New basket item inserted for userId={}, bookId={}, quantity={}", userId, bookId, delta);
            return true;
        } catch (SQLException e) {
            logger.error("Database error in addOrUpdateQuantity() for userId={}, bookId={}, delta={}", userId, bookId, delta, e);
            return false;
        }
    }

    /**
     * Sets the exact quantity of a basket item for a given user and book.
     * If the new quantity is zero or negative, the item is deleted.
     *
     * @param userId      the ID of the user whose basket is being modified
     * @param bookId      the ID of the book to update in the basket
     * @param newQuantity the new quantity to set
     * @return {@code true} if the operation succeeded, {@code false} otherwise
     */
    @Override
    public boolean setQuantity(Long userId, Long bookId, int newQuantity) {
        if (newQuantity <= 0) {
            logger.debug("New quantity <= 0; deleting item for userId={}, bookId={}", userId, bookId);
            return deleteItem(userId, bookId);
        }
        String sql = "UPDATE basket_items SET quantity = ? WHERE user_id = ? AND book_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setLong(2, userId);
            ps.setLong(3, bookId);
            logger.debug("Executing SET QUANTITY for basket_items: userId={}, bookId={}, quantity={}", userId, bookId, newQuantity);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.debug("Quantity set to {} for userId={}, bookId={}", newQuantity, userId, bookId);
            } else {
                logger.warn("Failed to set quantity for userId={}, bookId={}", userId, bookId);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Database error in setQuantity() for userId={}, bookId={}, newQuantity={}", userId, bookId, newQuantity, e);
            return false;
        }
    }

    /**
     * Retrieves all basket items for a given user.
     *
     * @param userId the ID of the user whose basket items are retrieved
     * @return a list of {@link BasketItem} instances, or an empty list if none found or on error
     */
    @Override
    public List<BasketItem> findByUserId(Long userId) {
        String sql = "SELECT id, user_id, book_id, quantity FROM basket_items WHERE user_id = ? ORDER BY id";
        List<BasketItem> result = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            logger.debug("Executing SELECT for basket_items by userId={}", userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BasketItem item = new BasketItem();
                    item.setId(rs.getLong("id"));
                    item.setUserId(rs.getLong("user_id"));
                    item.setBookId(rs.getLong("book_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    result.add(item);
                }
            }
            logger.debug("Fetched {} basket items for userId={}", result.size(), userId);
        } catch (SQLException e) {
            logger.error("Database error in findByUserId() for userId={}", userId, e);
        }
        return result;
    }

    /**
     * Deletes a specific basket item for a user and book.
     *
     * @param userId the ID of the user whose basket item is to be deleted
     * @param bookId the ID of the book to remove from the basket
     * @return {@code true} if the deletion succeeded, {@code false} otherwise
     */
    @Override
    public boolean deleteItem(Long userId, Long bookId) {
        String sql = "DELETE FROM basket_items WHERE user_id = ? AND book_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, bookId);
            logger.debug("Executing DELETE for basket_items: userId={}, bookId={}", userId, bookId);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                logger.debug("Deleted basket item for userId={}, bookId={}", userId, bookId);
            } else {
                logger.warn("No basket item found to delete for userId={}, bookId={}", userId, bookId);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Database error in deleteItem() for userId={}, bookId={}", userId, bookId, e);
            return false;
        }
    }

    /**
     * Deletes all basket items for a given user.
     *
     * @param userId the ID of the user whose basket items are to be deleted
     * @return {@code true} if one or more rows were deleted, {@code false} otherwise
     */
    @Override
    public boolean deleteAllByUserId(Long userId) {
        String sql = "DELETE FROM basket_items WHERE user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            logger.debug("Executing DELETE ALL for basket_items: userId={}", userId);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                logger.debug("Deleted all basket items for userId={}", userId);
            } else {
                logger.warn("No basket items found to delete for userId={}", userId);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Database error in deleteAllByUserId() for userId={}", userId, e);
            return false;
        }
    }
}
