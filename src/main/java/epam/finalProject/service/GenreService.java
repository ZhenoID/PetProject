package epam.finalProject.service;

import epam.finalProject.entity.Genre;

import java.util.List;

public interface GenreService {
    boolean save(Genre genre);

    List<Genre> findAll();

    Genre findById(Long id);
}
