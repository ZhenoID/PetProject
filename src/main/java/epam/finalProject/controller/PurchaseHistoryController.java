package epam.finalProject.controller;

import epam.finalProject.entity.Book;
import epam.finalProject.entity.PurchaseHistory;
import epam.finalProject.entity.User;
import epam.finalProject.service.BookService;
import epam.finalProject.service.PurchaseHistoryService;
import epam.finalProject.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for displaying a user’s purchase history.
 * Provides an endpoint to view all past purchases of the authenticated user.
 */
@Controller
public class PurchaseHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseHistoryController.class);

    private final PurchaseHistoryService historyService;
    private final BookService bookService;
    private final UserService userService;

    /**
     * Constructs a PurchaseHistoryController with the given services.
     *
     * @param historyService service layer for retrieving purchase history records
     * @param bookService    service layer for retrieving book details
     * @param userService    service layer for retrieving user information
     */
    public PurchaseHistoryController(PurchaseHistoryService historyService, BookService bookService, UserService userService) {
        this.historyService = historyService;
        this.bookService = bookService;
        this.userService = userService;
        logger.debug("PurchaseHistoryController initialized");
    }

    /**
     * Handles GET requests to "/purchase-history". Validates that the request is authenticated,
     * retrieves the purchase history for the current user, and adds it to the model.
     *
     * @param auth  the authentication object containing current user details
     * @param model the {@code Model} to which historyRows will be added
     * @return the name of the Thymeleaf template for displaying purchase history,
     * or a redirect to "/login" if the user is not authenticated or not found
     */
    @GetMapping("/purchase-history")
    public String viewHistory(Authentication auth, Model model) {
        if (auth == null || auth.getName() == null) {
            logger.warn("Unauthenticated access attempt to /purchase-history");
            return "redirect:/login";
        }

        String username = auth.getName();
        logger.debug("GET /purchase-history requested by user='{}'", username);

        User user = userService.getByUsername(username);
        if (user == null) {
            logger.warn("Authenticated user '{}' not found in database. Redirecting to /login", username);
            return "redirect:/login";
        }

        List<PurchaseHistory> raw = historyService.getByUserId(user.getId());
        logger.debug("Fetched {} purchase history records for userId={}", raw.size(), user.getId());

        List<Object[]> historyRows = raw.stream().map(ph -> {
            Book book = bookService.findById(ph.getBookId());
            if (book == null) {
                logger.warn("Book with id={} not found for purchase record id={}", ph.getBookId(), ph.getId());
            }
            return new Object[]{book, ph.getQuantity(), ph.getPurchaseDate()};
        }).collect(Collectors.toList());

        model.addAttribute("historyRows", historyRows);
        logger.debug("Added historyRows to model and returning purchase-history view");
        return "purchase-history";
    }
}
