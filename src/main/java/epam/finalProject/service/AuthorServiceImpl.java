package epam.finalProject.service;

import epam.finalProject.DAO.AuthorDao;
import epam.finalProject.DAO.AuthorDaoImpl;
import epam.finalProject.entity.Author;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for {@link Author} operations.
 * Delegates CRUD operations to the underlying {@link AuthorDao}.
 */
@Service
public class AuthorServiceImpl implements AuthorService {
    private static final Logger logger = LoggerFactory.getLogger(AuthorServiceImpl.class);

    private final AuthorDao authorDao;

    /**
     * Constructs an AuthorServiceImpl using a default {@link AuthorDaoImpl}.
     */
    public AuthorServiceImpl() {
        this(new AuthorDaoImpl());
        logger.debug("AuthorServiceImpl initialized with default AuthorDaoImpl");
    }

    /**
     * Constructs an AuthorServiceImpl using the specified {@link AuthorDao}.
     *
     * @param authorDao the DAO to delegate operations to
     */
    public AuthorServiceImpl(AuthorDao authorDao) {
        this.authorDao = authorDao;
        logger.debug("AuthorServiceImpl initialized with provided AuthorDao");
    }

    /**
     * Saves a new {@link Author}.
     *
     * @param author the Author to save
     * @return {@code true} if saved successfully, {@code false} otherwise
     */
    @Override
    public boolean save(Author author) {
        logger.debug("save() called for author name='{}'", author.getName());
        boolean result = authorDao.save(author);
        if (result) {
            logger.debug("Author saved with id={} name='{}'", author.getId(), author.getName());
        } else {
            logger.warn("Author not saved (might already exist): name='{}'", author.getName());
        }
        return result;
    }

    /**
     * Updates an existing {@link Author}.
     *
     * @param author the Author with updated fields
     * @return {@code true} if updated successfully, {@code false} otherwise
     */
    @Override
    public boolean update(Author author) {
        logger.debug("update() called for author id={}, name='{}'", author.getId(), author.getName());
        boolean result = authorDao.update(author);
        if (result) {
            logger.debug("Author updated successfully: id={} name='{}'", author.getId(), author.getName());
        } else {
            logger.warn("Author update failed for id={}", author.getId());
        }
        return result;
    }

    /**
     * Deletes the {@link Author} with the specified ID.
     *
     * @param id the ID of the Author to delete
     * @return {@code true} if deleted successfully, {@code false} otherwise
     */
    @Override
    public boolean delete(Long id) {
        logger.debug("delete() called for author id={}", id);
        boolean result = authorDao.delete(id);
        if (result) {
            logger.debug("Author deleted successfully: id={}", id);
        } else {
            logger.warn("Author deletion failed for id={}", id);
        }
        return result;
    }

    /**
     * Finds an {@link Author} by its ID.
     *
     * @param id the ID of the Author to find
     * @return the Author if found, or {@code null} otherwise
     */
    @Override
    public Author findById(Long id) {
        logger.debug("findById() called for author id={}", id);
        Author author = authorDao.findById(id);
        if (author != null) {
            logger.debug("Author found: id={} name='{}'", author.getId(), author.getName());
        } else {
            logger.warn("No author found for id={}", id);
        }
        return author;
    }

    /**
     * Retrieves all {@link Author} records.
     *
     * @return a List of all Authors
     */
    @Override
    public List<Author> findAll() {
        logger.debug("findAll() called to retrieve all authors");
        List<Author> authors = authorDao.findAll();
        logger.debug("Number of authors retrieved: {}", (authors != null ? authors.size() : 0));
        return authors;
    }

    /**
     * Checks whether an {@link Author} with the given ID exists.
     *
     * @param id the ID to check
     * @return {@code true} if the author exists, {@code false} otherwise
     */
    @Override
    public boolean existsById(Long id) {
        logger.debug("existsById() called for author id={}", id);
        boolean exists = authorDao.existsById(id);
        logger.debug("Author exists check for id={} returned {}", id, exists);
        return exists;
    }
}
