package epam.finalProject.service;

import epam.finalProject.entity.Author;

import java.util.List;

public interface AuthorService {
    boolean save(Author author);

    boolean update(Author author);

    boolean delete(Long id);

    Author findById(Long id);

    List<Author> findAll();

    boolean existsById(Long id);

}
