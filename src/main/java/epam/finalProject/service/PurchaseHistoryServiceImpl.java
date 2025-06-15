package epam.finalProject.service;

import epam.finalProject.DAO.PurchaseHistoryDao;
import epam.finalProject.DAO.PurchaseHistoryDaoImpl;
import epam.finalProject.entity.PurchaseHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for {@link PurchaseHistory} operations.
 * Delegates saving and retrieval of purchase history records to {@link PurchaseHistoryDao}.
 */
@Service
public class PurchaseHistoryServiceImpl implements PurchaseHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseHistoryServiceImpl.class);

    private final PurchaseHistoryDao historyDao = new PurchaseHistoryDaoImpl();

    /**
     * Records a new purchase history entry.
     *
     * @param ph the {@link PurchaseHistory} record to save
     * @return {@code true} if the record was saved successfully, {@code false} otherwise
     */
    @Override
    public boolean record(PurchaseHistory ph) {
        logger.debug("record() called for userId={} bookId={} quantity={} date={}", ph.getUserId(), ph.getBookId(), ph.getQuantity(), ph.getPurchaseDate());
        boolean result = historyDao.save(ph);
        if (result) {
            logger.debug("PurchaseHistory saved successfully with id={}", ph.getId());
        } else {
            logger.warn("Failed to save PurchaseHistory for userId={} bookId={}", ph.getUserId(), ph.getBookId());
        }
        return result;
    }

    /**
     * Retrieves all purchase history records for a given user, ordered by purchase date descending.
     *
     * @param userId the ID of the user whose purchase history is fetched
     * @return a {@link List} of {@link PurchaseHistory} entries; empty if none found or on error
     */
    @Override
    public List<PurchaseHistory> getByUserId(Long userId) {
        logger.debug("getByUserId() called for userId={}", userId);
        List<PurchaseHistory> historyList = historyDao.findByUserId(userId);
        logger.debug("Number of purchase history records retrieved for userId={}: {}", userId, historyList.size());
        return historyList;
    }
}
