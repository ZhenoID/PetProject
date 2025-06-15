package epam.finalProject.controller;

import epam.finalProject.entity.User;
import epam.finalProject.service.UserService;
import epam.finalProject.service.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller responsible for user profile management.
 * Provides endpoints to view and update user profile information and password.
 */
@Controller
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final UserService userService = new UserServiceImpl();

    /**
     * Handles GET requests to "/profile". Retrieves the authenticated user's data
     * and adds it to the model for display on the profile page.
     *
     * @param model     the {@code Model} to which the User object will be added
     * @param principal the {@code Principal} representing the authenticated user
     * @return the name of the profile view template
     */
    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        String username = principal.getName();
        logger.debug("GET /profile - loading profile for username='{}'", username);
        User user = userService.getByUsername(username);
        if (user == null) {
            logger.warn("User '{}' not found in database", username);
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    /**
     * Handles GET requests to "/profile/settings". Displays the settings page
     * where the user can change their password.
     *
     * @return the name of the profile settings view template
     */
    @GetMapping("/profile/settings")
    public String showSettings() {
        return "profileSettings";
    }

    /**
     * Handles POST requests to "/profile/settings". Validates and updates the user's password.
     *
     * @param oldPassword       the current password entered by the user
     * @param newPassword       the new password entered by the user
     * @param repeatNewPassword the new password confirmation
     * @param principal         the {@code Principal} representing the authenticated user
     * @param model             the {@code Model} to which success or error messages will be added
     * @return the name of the profile settings view template
     */
    @PostMapping("/profile/settings")
    public String updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword, @RequestParam String repeatNewPassword, Principal principal, Model model) {
        String username = principal.getName();
        logger.debug("POST /profile/settings - updating password for username='{}'", username);

        if (!newPassword.equals(repeatNewPassword)) {
            model.addAttribute("error", "New passwords do not match");
            return "profileSettings";
        }

        User user = userService.getByUsername(username);
        if (user == null || !BCrypt.checkpw(oldPassword, user.getPassword())) {
            model.addAttribute("error", "Old password is incorrect");
            return "profileSettings";
        }
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(hashedNewPassword);
        try {
            userService.updatePassword(user);
            model.addAttribute("message", "Password updated successfully");
            logger.debug("Password updated successfully for username='{}'", username);
        } catch (Exception e) {
            logger.error("Error updating password for username='{}': {}", username, e.getMessage(), e);
            model.addAttribute("error", "An error occurred while updating your password");
        }

        return "profileSettings";
    }
}
