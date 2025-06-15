package epam.finalProject.DAO;

import epam.finalProject.entity.Author;
import epam.finalProject.entity.Book;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * DAO interface for book data access operations.
 */
public interface BookDao {

    /**
     * Adds a book to the database.
     *
     * @param book the book to add
     * @return true if added
     */
    //boolean addBook(Book book);

    /**
     * Deletes a book by its ID.
     *
     * @param book the book to delete
     * @return true if deleted
     */
    boolean deleteBook(Book book);

    /**
     * Updates an existing book.
     *
     * @param book the book with updated values
     * @return true if updated
     */
    boolean changeBook(Book book);

    long count();
    /**
     * Returns a list of all books.
     *
     * @return list of books
     */

    List<Book> findAll(Pageable pageable);

    /**
     * Finds a book by its ID.
     *
     * @param id the book ID
     * @return the book object or null
     */
    Book findById(Long id);

    boolean saveBookWithAuthor(Book book, Author author);


    boolean decrementQuantity(Long bookId, int amount);


}

