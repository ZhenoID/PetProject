package epam.finalProject.controller.admin;

import epam.finalProject.entity.Author;
import epam.finalProject.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/admin/authors")
public class AdminAuthorController {

    private final AuthorService authorService;
    private static final Logger logger = LoggerFactory.getLogger(AdminAuthorController.class);

    @Autowired
    public AdminAuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping("/add")
    public String showAddAuthorForm(Model model) {
        logger.debug("GET /admin/authors/add — showing add author form");
        model.addAttribute("author", new Author());
        return "admin/add-author";
    }

    @PostMapping("/add")
    public String addAuthor(@ModelAttribute Author author) {
        try {
            authorService.save(author);
            logger.info("Author is saved: id={}, name='{}'", author.getId(), author.getName());
        } catch (Exception e) {
            logger.error("error with saving an author: name='{}'. Детали: {}", author.getName(), e.getMessage(), e);
        }
        return "redirect:/admin/books/add";
    }
}
