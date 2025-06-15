package epam.finalProject;

import epam.finalProject.DAO.BasketDao;
import epam.finalProject.DAO.BookDao;
import epam.finalProject.DAO.PurchaseHistoryDao;
import epam.finalProject.entity.BasketItem;
import epam.finalProject.entity.Book;
import epam.finalProject.entity.PurchaseHistory;
import epam.finalProject.service.BasketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BasketServiceImplTest {

    @Mock
    private BasketDao basketDao;

    @Mock
    private BookDao bookDao;

    @Mock
    private PurchaseHistoryDao historyDao;

    @InjectMocks
    private BasketServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new BasketServiceImpl();
        ReflectionTestUtils.setField(service, "basketDao", basketDao);
        ReflectionTestUtils.setField(service, "bookDao", bookDao);
        ReflectionTestUtils.setField(service, "historyDao", historyDao);
    }

    @Test
    void changeQuantity_decreaseBelowOne_returnsFalse() {
        Long userId = 1L, bookId = 2L;
        BasketItem item = new BasketItem();
        item.setUserId(userId);
        item.setBookId(bookId);
        item.setQuantity(1);
        when(basketDao.findByUserId(userId)).thenReturn(List.of(item));

        boolean result = service.changeQuantity(userId, bookId, -1);

        assertFalse(result);
        verify(basketDao, never()).addOrUpdateQuantity(anyLong(), anyLong(), anyInt());
    }

    @Test
    void changeQuantity_bookNotFound_returnsFalse() {
        Long userId = 1L, bookId = 2L;
        BasketItem item = new BasketItem();
        item.setUserId(userId);
        item.setBookId(bookId);
        item.setQuantity(1);
        when(basketDao.findByUserId(userId)).thenReturn(List.of(item));
        when(bookDao.findById(bookId)).thenReturn(null);

        boolean result = service.changeQuantity(userId, bookId, 1);

        assertFalse(result);
        verify(basketDao, never()).addOrUpdateQuantity(anyLong(), anyLong(), anyInt());
    }

    @Test
    void changeQuantity_insufficientStock_returnsFalse() {
        Long userId = 1L, bookId = 2L;
        BasketItem item = new BasketItem();
        item.setUserId(userId);
        item.setBookId(bookId);
        item.setQuantity(1);
        when(basketDao.findByUserId(userId)).thenReturn(List.of(item));
        Book book = new Book();
        book.setId(bookId);
        book.setQuantity(0);
        when(bookDao.findById(bookId)).thenReturn(book);

        boolean result = service.changeQuantity(userId, bookId, 1);

        assertFalse(result);
        verify(basketDao, never()).addOrUpdateQuantity(anyLong(), anyLong(), anyInt());
    }

    @Test
    void changeQuantity_success_returnsTrue() {
        Long userId = 1L, bookId = 2L;
        BasketItem item = new BasketItem();
        item.setUserId(userId);
        item.setBookId(bookId);
        item.setQuantity(1);
        when(basketDao.findByUserId(userId)).thenReturn(List.of(item));
        Book book = new Book();
        book.setId(bookId);
        book.setQuantity(5);
        when(bookDao.findById(bookId)).thenReturn(book);
        when(basketDao.addOrUpdateQuantity(userId, bookId, 2)).thenReturn(true);

        boolean result = service.changeQuantity(userId, bookId, 2);

        assertTrue(result);
        verify(basketDao).addOrUpdateQuantity(userId, bookId, 2);
    }

    @Test
    void setQuantity_bookNotFound_returnsFalse() {
        Long userId = 1L, bookId = 3L;
        when(bookDao.findById(bookId)).thenReturn(null);

        assertFalse(service.setQuantity(userId, bookId, 5));
        verify(basketDao, never()).setQuantity(anyLong(), anyLong(), anyInt());
    }

    @Test
    void setQuantity_insufficientStock_returnsFalse() {
        Long userId = 1L, bookId = 3L;
        Book book = new Book();
        book.setId(bookId);
        book.setQuantity(2);
        when(bookDao.findById(bookId)).thenReturn(book);

        assertFalse(service.setQuantity(userId, bookId, 5));
        verify(basketDao, never()).setQuantity(anyLong(), anyLong(), anyInt());
    }

    @Test
    void setQuantity_negative_setsZero() {
        Long userId = 1L, bookId = 3L;
        Book book = new Book();
        book.setId(bookId);
        book.setQuantity(10);
        when(bookDao.findById(bookId)).thenReturn(book);
        when(basketDao.setQuantity(userId, bookId, 0)).thenReturn(true);

        assertTrue(service.setQuantity(userId, bookId, -5));
        verify(basketDao).setQuantity(userId, bookId, 0);
    }

    @Test
    void setQuantity_success_returnsTrue() {
        Long userId = 1L, bookId = 3L;
        Book book = new Book();
        book.setId(bookId);
        book.setQuantity(10);
        when(bookDao.findById(bookId)).thenReturn(book);
        when(basketDao.setQuantity(userId, bookId, 4)).thenReturn(true);

        assertTrue(service.setQuantity(userId, bookId, 4));
        verify(basketDao).setQuantity(userId, bookId, 4);
    }

    @Test
    void removeItem_success() {
        Long userId = 1L, bookId = 4L;
        when(basketDao.deleteItem(userId, bookId)).thenReturn(true);

        assertTrue(service.removeItem(userId, bookId));
        verify(basketDao).deleteItem(userId, bookId);
    }

    @Test
    void removeItem_failure() {
        Long userId = 1L, bookId = 4L;
        when(basketDao.deleteItem(userId, bookId)).thenReturn(false);

        assertFalse(service.removeItem(userId, bookId));
    }

    @Test
    void clearBasket_success() {
        Long userId = 2L;
        when(basketDao.deleteAllByUserId(userId)).thenReturn(true);

        assertTrue(service.clearBasket(userId));
        verify(basketDao).deleteAllByUserId(userId);
    }

    @Test
    void clearBasket_failure() {
        Long userId = 2L;
        when(basketDao.deleteAllByUserId(userId)).thenReturn(false);

        assertFalse(service.clearBasket(userId));
    }

    @Test
    void getBasketItems_returnsList() {
        Long userId = 3L;
        List<BasketItem> items = new ArrayList<>();
        when(basketDao.findByUserId(userId)).thenReturn(items);

        assertSame(items, service.getBasketItems(userId));
    }

    @Test
    void confirmAll_empty_returnsTrue() {
        Long userId = 4L;
        when(basketDao.findByUserId(userId)).thenReturn(List.of());

        assertTrue(service.confirmAll(userId));
    }

    @Test
    void confirmAll_bookNotFound_returnsFalse() {
        Long userId = 5L;
        BasketItem item = new BasketItem();
        item.setUserId(userId);
        item.setBookId(6L);
        item.setQuantity(1);
        when(basketDao.findByUserId(userId)).thenReturn(List.of(item));
        when(bookDao.findById(6L)).thenReturn(null);

        assertFalse(service.confirmAll(userId));
    }

    @Test
    void confirmAll_insufficientStock_returnsFalse() {
        Long userId = 5L;
        BasketItem item = new BasketItem();
        item.setUserId(userId);
        item.setBookId(6L);
        item.setQuantity(5);
        when(basketDao.findByUserId(userId)).thenReturn(List.of(item));
        Book book = new Book();
        book.setId(6L);
        book.setQuantity(2);
        when(bookDao.findById(6L)).thenReturn(book);

        assertFalse(service.confirmAll(userId));
    }

    @Test
    void confirmAll_decrementFailure_returnsFalse() {
        Long userId = 5L;
        BasketItem item = new BasketItem();
        item.setUserId(userId);
        item.setBookId(6L);
        item.setQuantity(1);
        when(basketDao.findByUserId(userId)).thenReturn(List.of(item));
        Book book = new Book();
        book.setId(6L);
        book.setQuantity(10);
        when(bookDao.findById(6L)).thenReturn(book);
        when(bookDao.decrementQuantity(6L, 1)).thenReturn(false);

        assertFalse(service.confirmAll(userId));
    }

    @Test
    void confirmAll_historySaveFailure_returnsFalse() {
        Long userId = 5L;
        BasketItem item = new BasketItem();
        item.setUserId(userId);
        item.setBookId(6L);
        item.setQuantity(1);
        when(basketDao.findByUserId(userId)).thenReturn(List.of(item));
        Book book = new Book();
        book.setId(6L);
        book.setQuantity(10);
        when(bookDao.findById(6L)).thenReturn(book);
        when(bookDao.decrementQuantity(6L, 1)).thenReturn(true);
        when(historyDao.save(any(PurchaseHistory.class))).thenReturn(false);

        assertFalse(service.confirmAll(userId));
    }

    @Test
    void confirmAll_removeFailure_returnsFalse() {
        Long userId = 5L;
        BasketItem item = new BasketItem();
        item.setUserId(userId);
        item.setBookId(6L);
        item.setQuantity(1);
        when(basketDao.findByUserId(userId)).thenReturn(List.of(item));
        Book book = new Book();
        book.setId(6L);
        book.setQuantity(10);
        when(bookDao.findById(6L)).thenReturn(book);
        when(bookDao.decrementQuantity(6L, 1)).thenReturn(true);
        when(historyDao.save(any(PurchaseHistory.class))).thenReturn(true);
        when(basketDao.deleteItem(userId, 6L)).thenReturn(false);

        assertFalse(service.confirmAll(userId));
    }

    @Test
    void confirmAll_success_returnsTrue() {
        Long userId = 5L;
        BasketItem item = new BasketItem();
        item.setUserId(userId);
        item.setBookId(6L);
        item.setQuantity(1);
        when(basketDao.findByUserId(userId)).thenReturn(List.of(item));
        Book book = new Book();
        book.setId(6L);
        book.setQuantity(10);
        when(bookDao.findById(6L)).thenReturn(book);
        when(bookDao.decrementQuantity(6L, 1)).thenReturn(true);
        when(historyDao.save(any(PurchaseHistory.class))).thenReturn(true);
        when(basketDao.deleteItem(userId, 6L)).thenReturn(true);

        assertTrue(service.confirmAll(userId));
    }
}
