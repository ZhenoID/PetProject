package epam.finalProject.controller;

import epam.finalProject.entity.BasketItem;
import epam.finalProject.entity.Book;
import epam.finalProject.entity.User;
import epam.finalProject.service.BasketService;
import epam.finalProject.service.BookService;
import epam.finalProject.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing the user's shopping basket.
 * Provides endpoints to view the basket, change item quantities, remove items,
 * set exact quantities, and confirm the entire basket purchase.
 */
@Controller
@RequestMapping("/basket")
public class BasketController {

    private static final Logger logger = LoggerFactory.getLogger(BasketController.class);

    private final BasketService basketService;
    private final UserService userService;
    private final BookService bookService;

    /**
     * Constructs a BasketController with the specified services.
     *
     * @param basketService the service layer for basket-related operations
     * @param userService   the service layer for user-related operations
     * @param bookService   the service layer for book-related operations
     */
    public BasketController(BasketService basketService, UserService userService, BookService bookService) {
        this.basketService = basketService;
        this.userService = userService;
        this.bookService = bookService;
        logger.debug("BasketController initialized");
    }

    /**
     * Adds or subtracts a given delta (e.g., +1 or -1) to the quantity of a book in the user's basket.
     * Redirects back to the basket view after the operation.
     *
     * @param bookId the ID of the book whose quantity will be changed
     * @param delta  the amount to change the quantity by (positive to increase, negative to decrease)
     * @param auth   the authentication object containing current user details
     * @return redirect to "/basket"
     */
    @PostMapping("/change/{bookId}/{delta}")
    public String changeQuantity(@PathVariable Long bookId, @PathVariable int delta, Authentication auth) {
        String username = (auth != null) ? auth.getName() : "anonymous";
        logger.debug("POST /basket/change/{}/{} - user='{}' changing quantity by {}", bookId, delta, username, delta);
        if (auth != null) {
            User user = userService.getByUsername(username);
            if (user != null) {
                boolean result = basketService.changeQuantity(user.getId(), bookId, delta);
                if (result) {
                    logger.debug("Quantity changed successfully for userId={} bookId={} delta={}", user.getId(), bookId, delta);
                } else {
                    logger.warn("Failed to change quantity for userId={} bookId={} delta={}", user.getId(), bookId, delta);
                }
            } else {
                logger.warn("User not found: '{}'", username);
            }
        } else {
            logger.warn("Unauthenticated request to change basket quantity");
        }
        return "redirect:/basket";
    }

    /**
     * Sets the exact quantity of a book in the user's basket (e.g., from a numeric input field).
     * Redirects back to the basket view after the operation.
     *
     * @param bookId   the ID of the book whose quantity will be set
     * @param quantity the exact quantity to set
     * @param auth     the authentication object containing current user details
     * @return redirect to "/basket"
     */
    @PostMapping("/set/{bookId}")
    public String setQuantity(@PathVariable Long bookId, @RequestParam int quantity, Authentication auth) {
        String username = (auth != null) ? auth.getName() : "anonymous";
        logger.debug("POST /basket/set/{} - user='{}' setting quantity to {}", bookId, username, quantity);
        if (auth != null) {
            User user = userService.getByUsername(username);
            if (user != null) {
                boolean result = basketService.setQuantity(user.getId(), bookId, quantity);
                if (result) {
                    logger.debug("Quantity set to {} for userId={} bookId={}", quantity, user.getId(), bookId);
                } else {
                    logger.warn("Failed to set quantity to {} for userId={} bookId={}", quantity, user.getId(), bookId);
                }
            } else {
                logger.warn("User not found: '{}'", username);
            }
        } else {
            logger.warn("Unauthenticated request to set basket quantity");
        }
        return "redirect:/basket";
    }

    /**
     * Removes an entire item (book) from the user's basket.
     * Redirects back to the basket view after removal.
     *
     * @param bookId the ID of the book to remove from the basket
     * @param auth   the authentication object containing current user details
     * @return redirect to "/basket"
     */
    @PostMapping("/remove/{bookId}")
    public String removeItem(@PathVariable Long bookId, Authentication auth) {
        String username = (auth != null) ? auth.getName() : "anonymous";
        logger.debug("POST /basket/remove/{} - user='{}' removing item from basket", bookId, username);
        if (auth != null) {
            User user = userService.getByUsername(username);
            if (user != null) {
                boolean result = basketService.removeItem(user.getId(), bookId);
                if (result) {
                    logger.debug("Item removed successfully for userId={} bookId={}", user.getId(), bookId);
                } else {
                    logger.warn("Failed to remove item for userId={} bookId={}", user.getId(), bookId);
                }
            } else {
                logger.warn("User not found: '{}'", username);
            }
        } else {
            logger.warn("Unauthenticated request to remove basket item");
        }
        return "redirect:/basket";
    }

    /**
     * Displays the current contents of the user's basket.
     * Loads each BasketItem, retrieves the corresponding Book, sets the Book's quantity to the BasketItem quantity,
     * and passes the list of Book objects to the view.
     *
     * @param auth  the authentication object containing current user details
     * @param model the model to which the list of books in the basket will be added
     * @return the name of the Thymeleaf template for displaying the basket
     */
    @GetMapping
    public String viewBasket(Authentication auth, Model model) {
        String username = (auth != null) ? auth.getName() : "anonymous";
        logger.debug("GET /basket - user='{}' viewing basket", username);
        List<BasketItem> items;
        if (auth != null) {
            User user = userService.getByUsername(username);
            if (user != null) {
                items = basketService.getBasketItems(user.getId());
                logger.debug("Fetched {} items from basket for userId={}", items.size(), user.getId());
            } else {
                logger.warn("User not found: '{}'", username);
                items = List.of();
            }
        } else {
            logger.warn("Unauthenticated request to view basket");
            items = List.of();
        }

        List<Book> booksInBasket = items.stream().map(it -> {
            Book b = bookService.findById(it.getBookId());
            if (b != null) {
                b.setQuantity(it.getQuantity());
            } else {
                logger.warn("Book not found for basketItem: bookId={}", it.getBookId());
            }
            return b;
        }).collect(Collectors.toList());

        model.addAttribute("booksInBasket", booksInBasket);
        return "basket";
    }

    /**
     * Confirms the purchase of all items in the user's basket.
     * If successful, redirects to the purchase history page; otherwise, stays on the basket page with an error message.
     *
     * @param auth  the authentication object containing current user details
     * @param model the model to which error messages can be added
     * @return redirect to "/purchase-history" on success, or the basket view on failure
     */
    @PostMapping("/confirm")
    public String confirmAll(Authentication auth, Model model) {
        String username = (auth != null) ? auth.getName() : "anonymous";
        logger.debug("POST /basket/confirm - user='{}' confirming entire basket purchase", username);

        boolean success = false;
        if (auth != null) {
            User user = userService.getByUsername(username);
            if (user != null) {
                success = basketService.confirmAll(user.getId());
                if (success) {
                    logger.debug("Basket purchase confirmed for userId={}", user.getId());
                } else {
                    logger.warn("Basket purchase failed for userId={}", user.getId());
                }
            } else {
                logger.warn("User not found: '{}'", username);
            }
        } else {
            logger.warn("Unauthenticated request to confirm basket purchase");
        }

        if (!success) {
            model.addAttribute("error", "An error occurred: please check that sufficient quantities are available.");
            return viewBasket(auth, model);
        }
        return "redirect:/purchase-history";
    }
}
