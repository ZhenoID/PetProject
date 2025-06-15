package epam.finalProject.controller;

import epam.finalProject.entity.Book;
import epam.finalProject.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller to handle requests for viewing the list of all books.
 * Provides an endpoint to fetch and display all books to the user.
 */
@Controller
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    /**
     * Constructs a BookController with the specified BookService.
     *
     * @param bookService service layer for book-related operations
     */
    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
        logger.debug("BookController initialized");
    }

    /**
     * Handles GET requests to "/books". Retrieves all books from the service
     * and adds them to the model for display in the "books" view.
     *
     * @param model Spring MVC model to which the list of books will be added
     * @return the name of the Thymeleaf template for displaying all books
     */
    @GetMapping("/books")
    public String showBooks(@RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "10") int size, Model model) {
        logger.debug("showBooks() page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> booksPage = bookService.findAll(pageable);
        model.addAttribute("booksPage", booksPage);
        return "books";
    }

}
