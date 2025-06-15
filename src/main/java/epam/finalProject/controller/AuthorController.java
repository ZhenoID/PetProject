package epam.finalProject.controller;

import epam.finalProject.entity.Author;
import epam.finalProject.service.AuthorService;
import epam.finalProject.service.AuthorServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling public-facing author operations.
 * Provides endpoints to list all authors, display a form to create or edit an author,
 * save/upsert an author, and delete an author.
 */
@Controller
@RequestMapping("/authors")
public class AuthorController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);

    private final AuthorService authorService = new AuthorServiceImpl();

    /**
     * Handles GET requests to "/authors".
     * Retrieves all authors and adds them to the model for display.
     *
     * @param model Spring MVC model to which the list of authors will be added
     * @return the name of the Thymeleaf template for listing authors
     */
    @GetMapping
    public String listAuthors(Model model) {
        logger.debug("GET /authors - fetching list of authors");
        List<Author> authors = authorService.findAll();
        logger.debug("Number of authors fetched: {}", authors.size());
        model.addAttribute("authors", authors);
        return "authors";
    }

    /**
     * Handles GET requests to "/authors/new".
     * Displays a form for creating a new author.
     *
     * @param model Spring MVC model to which a new Author object will be added
     * @return the name of the Thymeleaf template for the author form
     */
    @GetMapping("/new")
    public String showAddForm(Model model) {
        logger.debug("GET /authors/new - displaying form to create a new author");
        model.addAttribute("author", new Author());
        return "authorForm";
    }

    /**
     * Handles POST requests to "/authors/save".
     * If the Author has no ID, creates a new record; otherwise, updates the existing one.
     * After saving or updating, redirects back to the authors list.
     *
     * @param author Author object populated from form data
     * @return redirect to "/authors" after save or update
     */
    @PostMapping("/save")
    public String saveAuthor(@ModelAttribute Author author) {
        if (author.getId() == null) {
            logger.debug("POST /authors/save - creating new author: name='{}'", author.getName());
            authorService.save(author);
            logger.debug("New author created with generated ID={}", author.getId());
        } else {
            logger.debug("POST /authors/save - updating existing author: id={}, name='{}'", author.getId(), author.getName());
            authorService.update(author);
            logger.debug("Author updated: id={}", author.getId());
        }
        return "redirect:/authors";
    }

    /**
     * Handles GET requests to "/authors/edit/{id}".
     * Retrieves the author with the specified ID and adds it to the model for editing.
     *
     * @param id    the ID of the author to edit
     * @param model Spring MVC model to which the Author object will be added
     * @return the name of the Thymeleaf template for the author form
     */
    @GetMapping("/edit/{id}")
    public String editAuthor(@PathVariable Long id, Model model) {
        logger.debug("GET /authors/edit/{} - fetching author for edit", id);
        Author author = authorService.findById(id);
        model.addAttribute("author", author);
        return "authorForm";
    }

    /**
     * Handles GET requests to "/authors/delete/{id}".
     * Deletes the author with the given ID and redirects back to the authors list.
     *
     * @param id the ID of the author to delete
     * @return redirect to "/authors" after deletion
     */
    @GetMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable Long id) {
        logger.debug("GET /authors/delete/{} - deleting author", id);
        authorService.delete(id);
        logger.debug("Author deleted: id={}", id);
        return "redirect:/authors";
    }
}
