package epam.finalProject.service;

import epam.finalProject.entity.BasketItem;

import java.util.List;

public interface BasketService {

    boolean changeQuantity(Long userId, Long bookId, int delta);

    boolean setQuantity(Long userId, Long bookId, int newQuantity);

    boolean removeItem(Long userId, Long bookId);

    boolean clearBasket(Long userId);

    List<BasketItem> getBasketItems(Long userId);

    boolean confirmAll(Long userId);
}
