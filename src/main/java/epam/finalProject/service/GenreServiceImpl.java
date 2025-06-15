package epam.finalProject.service;

import epam.finalProject.DAO.GenreDao;
import epam.finalProject.DAO.GenreDaoImpl;
import epam.finalProject.entity.Genre;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for {@link Genre} operations.
 * Delegates CRUD operations to the underlying {@link GenreDao}.
 */
@Service
public class GenreServiceImpl implements GenreService {
    private static final Logger logger = LoggerFactory.getLogger(GenreServiceImpl.class);

    private final GenreDao genreDao = new GenreDaoImpl();

    /**
     * Saves a new {@link Genre}.
     *
     * @param genre the Genre to save
     * @return {@code true} if saved successfully, {@code false} otherwise
     */
    @Override
    public boolean save(Genre genre) {
        logger.debug("save() called for genre name='{}'", genre.getName());
        boolean result = genreDao.save(genre);
        if (result) {
            logger.debug("Genre saved successfully: id={} name='{}'", genre.getId(), genre.getName());
        } else {
            logger.warn("Genre save failed for name='{}'", genre.getName());
        }
        return result;
    }

    /**
     * Retrieves all {@link Genre} records.
     *
     * @return a List of all Genres
     */
    @Override
    public List<Genre> findAll() {
        logger.debug("findAll() called to retrieve all genres");
        List<Genre> genres = genreDao.findAll();
        logger.debug("Number of genres retrieved: {}", genres.size());
        return genres;
    }

    /**
     * Finds a {@link Genre} by its ID.
     *
     * @param id the ID of the Genre to find
     * @return the Genre if found, or {@code null} otherwise
     */
    @Override
    public Genre findById(Long id) {
        logger.debug("findById() called for genre id={}", id);
        Genre genre = genreDao.findById(id);
        if (genre != null) {
            logger.debug("Genre found: id={} name='{}'", genre.getId(), genre.getName());
        } else {
            logger.warn("No genre found for id={}", id);
        }
        return genre;
    }
}
