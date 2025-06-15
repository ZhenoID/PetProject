package epam.finalProject.controller.admin;

import epam.finalProject.entity.Genre;
import epam.finalProject.service.GenreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/genres")
@PreAuthorize("hasAnyAuthority('ADMIN','LIBRARIAN')")
public class AdminGenreController {
    private static final Logger logger = LoggerFactory.getLogger(AdminGenreController.class);
    private final GenreService genreService;

    public AdminGenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    /**
     * Shows the form of the creating new genre
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("genre", new Genre());
        return "admin/add-genre";
    }

    /**
     * Process post form of the creating new genre
     */
    @PostMapping("/add")
    public String addGenre(@ModelAttribute Genre genre) {
        logger.debug("POST /admin/genres/add — got new info about name='{}'", genre.getName());

        try {
            genreService.save(genre);
            logger.debug("the new genre is saved: id={}, name='{}'", genre.getId(), genre.getName());
        } catch (Exception e) {
            logger.error("Error with adding a new genre: name='{}'. details: {}", genre.getName(), e.getMessage(), e);
            return "admin/add-genre";
        }

        return "redirect:/admin/books/add";
    }


    /**
     * Shows the list of all genres
     */
    @GetMapping
    public String listGenres(Model model) {
        logger.debug("GET /admin/genres — taking list of genres");
        List<Genre> genres = genreService.findAll();
        logger.debug("Found {} genres", genres.size());
        model.addAttribute("genres", genres);
        return "admin/genre-list";
    }
}
