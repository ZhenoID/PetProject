package epam.finalProject.DAO;

import epam.finalProject.entity.BasketItem;

import java.util.List;

public interface BasketDao {

    boolean addOrUpdateQuantity(Long userId, Long bookId, int delta);

    boolean setQuantity(Long userId, Long bookId, int newQuantity);

    List<BasketItem> findByUserId(Long userId);

    boolean deleteItem(Long userId, Long bookId);

    boolean deleteAllByUserId(Long userId);
}
