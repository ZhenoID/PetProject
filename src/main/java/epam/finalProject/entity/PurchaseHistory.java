package epam.finalProject.entity;

import java.sql.Timestamp;

public class PurchaseHistory {
    private Long id;
    private Long userId;
    private Long bookId;
    private int quantity;
    private Timestamp purchaseDate;

    public PurchaseHistory() {
    }

    public PurchaseHistory(Long userId, Long bookId, int quantity, Timestamp purchaseDate) {
        this.userId = userId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Timestamp getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Timestamp purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}
