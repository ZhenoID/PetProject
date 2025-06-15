package epam.finalProject.controller.admin;

import epam.finalProject.entity.Author;
import epam.finalProject.entity.Book;
import epam.finalProject.exception.ResourceNotFoundException;
import epam.finalProject.service.AuthorService;
import epam.finalProject.service.AuthorServiceImpl;
import epam.finalProject.service.BookService;
import epam.finalProject.service.GenreService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@PreAuthorize("hasAnyAuthority('ADMIN','LIBRARIAN')")
@RequestMapping("/admin/books")
public class AdminBookController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final GenreService genreService;

    public AdminBookController(BookService bookService, AuthorService authorService, GenreService genreService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.genreService = genreService;
    }


    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "admin/add-book";
    }


    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book, Model model) {

        if (!authorService.existsById(book.getAuthorId())) {
            Author newAuthor = new Author();
            newAuthor.setId(book.getAuthorId());
            newAuthor.setName("Unknown Author " + book.getAuthorId());

            authorService.save(newAuthor);
        }

        Author author = authorService.findById(book.getAuthorId());

        if (bookService.saveBookWithAuthor(book, author)) {
            return "redirect:/books";
        }

        model.addAttribute("error", "Failed to add book.");
        model.addAttribute("book", book);
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "admin/add-book";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Book book = bookService.findById(id);
            model.addAttribute("book", book);
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());
            return "admin/edit-book";
        } catch (ResourceNotFoundException ex) {
            return "redirect:/books?notfound=true";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/books?error=true";
        }
    }


    @PostMapping("/edit")
    public String editBook(@ModelAttribute Book book, Model model) {
        if (bookService.changeBook(book)) {
            return "redirect:/books";
        }
        model.addAttribute("error", "Failed to update book.");
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "edit-book";
    }


}