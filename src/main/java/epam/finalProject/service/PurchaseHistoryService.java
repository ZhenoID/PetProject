package epam.finalProject.service;

import epam.finalProject.entity.PurchaseHistory;

import java.util.List;

public interface PurchaseHistoryService {
    boolean record(PurchaseHistory ph);

    List<PurchaseHistory> getByUserId(Long userId);
}
