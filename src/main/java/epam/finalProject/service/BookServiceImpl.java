package epam.finalProject.service;

import epam.finalProject.DAO.BookDao;
import epam.finalProject.DAO.BookDaoImpl;
import epam.finalProject.entity.Author;
import epam.finalProject.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service implementation for {@link Book} operations.
 * Delegates CRUD and transactional operations to the underlying {@link BookDao}.
 */
@Service
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookDao bookDao;

    /**
     * Constructs a BookServiceImpl using the default {@link BookDaoImpl}.
     */
    public BookServiceImpl() {
        this(new BookDaoImpl());
        logger.debug("BookServiceImpl initialized with default BookDaoImpl");
    }

    /**
     * Constructs a BookServiceImpl using the specified {@link BookDao}.
     *
     * @param bookDao the DAO to delegate operations to
     */
    public BookServiceImpl(BookDao bookDao) {
        this.bookDao = bookDao;
        logger.debug("BookServiceImpl initialized with provided BookDao");
    }

    /**
     * Deletes an existing {@link Book} from the system.
     *
     * @param book the Book to delete
     * @return {@code true} if deletion succeeded, {@code false} otherwise
     */
    @Override
    public boolean deleteBook(Book book) {
        logger.debug("deleteBook() called for book id={} title='{}'", book.getId(), book.getTitle());
        boolean result = bookDao.deleteBook(book);
        if (result) {
            logger.debug("Book deleted successfully: id={}", book.getId());
        } else {
            logger.warn("Book deletion failed for id={}", book.getId());
        }
        return result;
    }

    /**
     * Updates the fields of an existing {@link Book}.
     *
     * @param book the Book containing updated information
     * @return {@code true} if update succeeded, {@code false} otherwise
     */
    @Override
    public boolean changeBook(Book book) {
        logger.debug("changeBook() called for book id={} title='{}'", book.getId(), book.getTitle());
        boolean result = bookDao.changeBook(book);
        if (result) {
            logger.debug("Book updated successfully: id={}", book.getId());
        } else {
            logger.warn("Book update failed for id={}", book.getId());
        }
        return result;
    }

    /**
     * Retrieves all {@link Book} records from the system.
     *
     * @return a List of all Book entities
     */
    @Override
    public Page<Book> findAll(Pageable pageable) {
        logger.debug("findAll() called to retrieve all books");
        long total = bookDao.count();
        List<Book> books = bookDao.findAll(pageable);
        logger.debug("Number of books retrieved: {}", books.size());
        return new PageImpl<>(books, pageable, total);
    }

    /**
     * Finds a {@link Book} by its ID.
     *
     * @param id the ID of the Book to retrieve
     * @return the Book if found, or {@code null} otherwise
     */
    @Override
    public Book findById(Long id) {
        logger.debug("findById() called for book id={}", id);
        Book book = bookDao.findById(id);
        if (book != null) {
            logger.debug("Book found: id={} title='{}'", book.getId(), book.getTitle());
        } else {
            logger.warn("No book found for id={}", id);
        }
        return book;
    }

    /**
     * Saves a new {@link Book} along with its {@link Author} in a single transaction.
     * If the author does not already exist, it will be created.
     *
     * @param book   the Book entity to save
     * @param author the Author entity to associate with the book
     * @return {@code true} if the book (and author) were saved successfully, {@code false} otherwise
     */
    @Override
    public boolean saveBookWithAuthor(Book book, Author author) {
        logger.debug("saveBookWithAuthor() called for book title='{}' author='{}'", book.getTitle(), author.getName());
        boolean result = bookDao.saveBookWithAuthor(book, author);
        if (result) {
            logger.debug("Book and author saved successfully: bookId={} authorId={}", book.getId(), author.getId());
        } else {
            logger.warn("Failed to save book with author: title='{}' author='{}'", book.getTitle(), author.getName());
        }
        return result;
    }
}
