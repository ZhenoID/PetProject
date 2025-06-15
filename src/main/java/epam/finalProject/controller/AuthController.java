package epam.finalProject.controller;

import epam.finalProject.entity.User;
import epam.finalProject.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for user authentication and registration.
 * Handles showing registration and login forms, processing registration and login requests,
 * and performing logout.
 */
@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final UserDetailsService userDetailsService;

    /**
     * Constructs an AuthController with the given UserService and UserDetailsService.
     *
     * @param userService        service layer for user-related operations
     * @param userDetailsService Spring Security's service for loading user-specific data
     */
    @Autowired
    public AuthController(UserService userService, UserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        logger.debug("AuthController initialized");
    }

    /**
     * Displays the registration form.
     *
     * @param model model to which a new User object is added
     * @return the name of the Thymeleaf template for the registration page
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        logger.debug("GET /register - displaying registration form");
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Processes the registration form submission.
     * Attempts to register a new user. If successful, redirects to the login page.
     * If the username is already taken or an exception occurs, returns to the registration form with an error message.
     *
     * @param user  User object populated from form data
     * @param model model to which error messages can be added
     * @return redirect to login on success, or the registration template on failure
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user, BindingResult br, Model model) {
        logger.debug("POST /register - attempting to register user with username='{}'", user.getUsername());
        if (br.hasErrors()) {
            return "register";
        }
        try {
            boolean registered = userService.register(user);
            if (registered) {
                logger.debug("User '{}' registered successfully", user.getUsername());
                return "redirect:/login";
            } else {
                logger.warn("Registration failed: username '{}' already taken", user.getUsername());
                model.addAttribute("error", "Username already taken");
            }
        } catch (Exception e) {
            logger.error("Exception during registration for username='{}': {}", user.getUsername(), e.getMessage(), e);
            model.addAttribute("error", "Registration failed. Please try again later.");
        }
        return "register";
    }

    /**
     * Displays the login form.
     *
     * @return the name of the Thymeleaf template for the login page
     */
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "logout", required = false) String logout, Model model) {
        if (error != null) {
            logger.warn("Login failed: bad credentials");
            model.addAttribute("errorKey", "error.login");
        }
        return "login";
    }

    /**
     * Processes the login form submission.
     * If authentication succeeds, sets up the Spring Security context and redirects to the home page.
     * Otherwise, returns to the login form with an error message.
     *
     * @param username username submitted by the user
     * @param password password submitted by the user
     * @param session  HTTP session in which to store authentication details
     * @param model    model to which error messages can be added
     * @return redirect to home on success, or the login template on failure
     */
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        logger.debug("POST /login - attempting login for username='{}'", username);
        if (userService.authenticate(username, password)) {
            logger.debug("Authentication successful for username='{}'", username);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);

            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            session.setAttribute("username", username);

            return "redirect:/home";
        }

        logger.warn("Authentication failed for username='{}'", username);
        model.addAttribute("errorKey", "error.login");
        return "login";
    }

    /**
     * Displays the home page for authenticated users.
     *
     * @return the name of the Thymeleaf template for the home page
     */
    @GetMapping("/home")
    public String home() {
        logger.debug("GET /home - displaying home page");
        return "home";
    }

    /**
     * Logs out the current user by invalidating the session and redirects to the login page.
     *
     * @param session HTTP session to invalidate
     * @return redirect to login
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        logger.debug("GET /logout - invalidating session and logging out");
        session.invalidate();
        return "redirect:/login";
    }
}
