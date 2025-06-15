package epam.finalProject.DAO;

import epam.finalProject.entity.Genre;

import java.sql.SQLException;
import java.util.List;

public interface GenreDao {
    boolean save(Genre genre);

    Genre findById(Long id);

    List<Genre> findAll();

    List<Genre> findByBookId(long bookId) throws SQLException;

}
