package epam.finalProject.entity;

public class FavoriteBook {
    private Long userId;
    private Long bookId;
    private boolean confirmed;

    public FavoriteBook() {
    }

    public FavoriteBook(Long userId, Long bookId, boolean confirmed) {
        this.userId = userId;
        this.bookId = bookId;
        this.confirmed = confirmed;
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

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}