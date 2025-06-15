package epam.finalProject.DAO;

import epam.finalProject.entity.Author;
import java.util.List;

public interface AuthorDao {
    boolean save(Author author);
    boolean update(Author author);
    boolean delete(Long id);
    Author findById(Long id);
    List<Author> findAll();
    boolean existsById(Long id);

}
