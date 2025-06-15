package epam.finalProject.service;

import epam.finalProject.DAO.BasketDao;
import epam.finalProject.DAO.BasketDaoImpl;
import epam.finalProject.DAO.BookDao;
import epam.finalProject.DAO.BookDaoImpl;
import epam.finalProject.DAO.PurchaseHistoryDao;
import epam.finalProject.DAO.PurchaseHistoryDaoImpl;
import epam.finalProject.entity.BasketItem;
import epam.finalProject.entity.PurchaseHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;

/**
 * Service implementation for managing the user’s shopping basket.
 * Provides methods to add/remove items, adjust quantities, clear the basket,
 * retrieve basket contents, and confirm all purchases.
 */
@Service
public class BasketServiceImpl implements BasketService {

    private static final Logger logger = LoggerFactory.getLogger(BasketServiceImpl.class);
    private DataSource ds;

    private final BasketDao basketDao = new BasketDaoImpl();
    private final BookDao bookDao = new BookDaoImpl();
    private final PurchaseHistoryDao historyDao = new PurchaseHistoryDaoImpl();

    /**
     * Adds or subtracts {@code delta} copies of a book in the user’s basket.
     * Will not allow quantity to drop below 1. Validates stock availability when increasing.
     *
     * @param userId the ID of the user whose basket is being modified
     * @param bookId the ID of the book to adjust
     * @param delta  the change in quantity (positive to increment, negative to decrement)
     * @return {@code true} if the basket was updated successfully, {@code false} otherwise
     */
    @Override
    public boolean changeQuantity(Long userId, Long bookId, int delta) {
        logger.debug("changeQuantity() called for userId={} bookId={} delta={}", userId, bookId, delta);

        int currentBasketQty = 0;
        List<BasketItem> items = basketDao.findByUserId(userId);
        for (BasketItem it : items) {
            if (it.getBookId().equals(bookId)) {
                currentBasketQty = it.getQuantity();
                break;
            }
        }
        logger.debug("Current basket quantity for userId={} bookId={} is {}", userId, bookId, currentBasketQty);

        int desiredQty = currentBasketQty + delta;

        if (delta < 0 && desiredQty < 1) {
            logger.warn("Attempt to reduce quantity below 1 for userId={} bookId={}", userId, bookId);
            return false;
        }

        if (delta > 0) {
            epam.finalProject.entity.Book book = bookDao.findById(bookId);
            if (book == null) {
                logger.error("Book not found for bookId={}", bookId);
                return false;
            }
            if (book.getQuantity() < delta) {
                logger.warn("Insufficient stock for bookId={} requested delta={} available={}", bookId, delta, book.getQuantity());
                return false;
            }
        }

        boolean result = basketDao.addOrUpdateQuantity(userId, bookId, delta);
        if (result) {
            logger.debug("Basket quantity updated for userId={} bookId={} delta={}", userId, bookId, delta);
        } else {
            logger.error("Failed to update basket quantity for userId={} bookId={} delta={}", userId, bookId, delta);
        }
        return result;
    }

    /**
     * Sets the exact quantity of a book in the user’s basket.
     * If {@code newQuantity} is less than zero, it is treated as zero (item removed).
     * Validates stock availability before setting.
     *
     * @param userId      the ID of the user whose basket is being modified
     * @param bookId      the ID of the book to set
     * @param newQuantity the exact quantity to set (non-negative)
     * @return {@code true} if the basket item was updated or removed successfully, {@code false} otherwise
     */
    @Override
    public boolean setQuantity(Long userId, Long bookId, int newQuantity) {
        logger.debug("setQuantity() called for userId={} bookId={} newQuantity={}", userId, bookId, newQuantity);

        if (newQuantity < 0) {
            newQuantity = 0;
        }

        epam.finalProject.entity.Book book = bookDao.findById(bookId);
        if (book == null) {
            logger.error("Book not found for bookId={}", bookId);
            return false;
        }
        if (book.getQuantity() < newQuantity) {
            logger.warn("Insufficient stock for bookId={} requested newQuantity={} available={}", bookId, newQuantity, book.getQuantity());
            return false;
        }

        boolean result = basketDao.setQuantity(userId, bookId, newQuantity);
        if (result) {
            logger.debug("Basket quantity set to {} for userId={} bookId={}", newQuantity, userId, bookId);
        } else {
            logger.error("Failed to set quantity to {} for userId={} bookId={}", newQuantity, userId, bookId);
        }
        return result;
    }

    /**
     * Removes a single book item entirely from the user’s basket.
     *
     * @param userId the ID of the user whose basket is being modified
     * @param bookId the ID of the book to remove
     * @return {@code true} if the item was removed, {@code false} otherwise
     */
    @Override
    public boolean removeItem(Long userId, Long bookId) {
        logger.debug("removeItem() called for userId={} bookId={}", userId, bookId);
        boolean result = basketDao.deleteItem(userId, bookId);
        if (result) {
            logger.debug("Removed basket item for userId={} bookId={}", userId, bookId);
        } else {
            logger.warn("No basket item to remove for userId={} bookId={}", userId, bookId);
        }
        return result;
    }

    /**
     * Clears all items from the user’s basket.
     *
     * @param userId the ID of the user whose basket is being cleared
     * @return {@code true} if items were deleted, {@code false} otherwise
     */
    @Override
    public boolean clearBasket(Long userId) {
        logger.debug("clearBasket() called for userId={}", userId);
        boolean result = basketDao.deleteAllByUserId(userId);
        if (result) {
            logger.debug("Cleared all basket items for userId={}", userId);
        } else {
            logger.warn("No basket items to clear for userId={}", userId);
        }
        return result;
    }

    /**
     * Retrieves all {@link BasketItem} records for the specified user.
     *
     * @param userId the ID of the user whose basket items are retrieved
     * @return a list of BasketItem objects; empty list if none found or on error
     */
    @Override
    public List<BasketItem> getBasketItems(Long userId) {
        logger.debug("getBasketItems() called for userId={}", userId);
        List<BasketItem> items = basketDao.findByUserId(userId);
        logger.debug("Found {} basket items for userId={}", items.size(), userId);
        return items;
    }

    /**
     * Confirms the purchase of all items currently in the user’s basket.
     * Checks stock availability for each item, then decrements book stock,
     * saves purchase records, and removes items from the basket.
     *
     * @param userId the ID of the user confirming the purchase
     * @return {@code true} if all items were successfully processed, {@code false} otherwise
     */
    public boolean confirmAll(Long userId) {
        logger.debug("confirmAll() called for userId={}", userId);

        List<BasketItem> items = basketDao.findByUserId(userId);
        logger.debug("Processing {} items for confirmation for userId={}", items.size(), userId);

        for (BasketItem it : items) {
            epam.finalProject.entity.Book book = bookDao.findById(it.getBookId());
            if (book == null) {
                logger.error("Book not found for bookId={} during confirmation", it.getBookId());
                return false;
            }
            if (book.getQuantity() < it.getQuantity()) {
                logger.warn("Insufficient stock for bookId={} requested={} available={}", it.getBookId(), it.getQuantity(), book.getQuantity());
                return false;
            }
        }

        for (BasketItem it : items) {
            boolean decremented = bookDao.decrementQuantity(it.getBookId(), it.getQuantity());
            if (!decremented) {
                logger.error("Failed to decrement stock for bookId={} quantity={}", it.getBookId(), it.getQuantity());
                return false;
            }
            logger.debug("Book stock decremented for bookId={} by {}", it.getBookId(), it.getQuantity());

            PurchaseHistory record = new PurchaseHistory(userId, it.getBookId(), it.getQuantity(), new Timestamp(System.currentTimeMillis()));
            boolean savedHistory = historyDao.save(record);
            if (!savedHistory) {
                logger.error("Failed to save purchase history for userId={} bookId={} quantity={}", userId, it.getBookId(), it.getQuantity());
                return false;
            }
            logger.debug("PurchaseHistory record saved: {}", record);

            boolean removed = basketDao.deleteItem(userId, it.getBookId());
            if (!removed) {
                logger.error("Failed to remove basket item during confirmation for userId={} bookId={}", userId, it.getBookId());
                return false;
            }
            logger.debug("Removed basket item for userId={} bookId={}", userId, it.getBookId());
        }

        logger.debug("All basket items confirmed successfully for userId={}", userId);
        return true;
    }
}
