package epam.finalProject.DAO;

import epam.finalProject.entity.PurchaseHistory;

import java.util.List;

public interface PurchaseHistoryDao {
    boolean save(PurchaseHistory record);

    List<PurchaseHistory> findByUserId(Long userId);
}
