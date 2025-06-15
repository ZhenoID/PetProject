package epam.finalProject.controller.admin;

import epam.finalProject.entity.User;
import epam.finalProject.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for managing user accounts in the admin panel.
 * Provides endpoints to list, edit roles, and delete users.
 * Access is restricted to users with the "ADMIN" authority.
 * Admins cannot modify or delete other admins (only themselves).
 */
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
        logger.debug("AdminUserController initialized");
    }

    @GetMapping
    public String listUsers(@RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "10") int size, Model model) {
        logger.debug("listUsers() page={} size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userService.findAll(pageable);
        model.addAttribute("usersPage", usersPage);
        return "admin/users";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        logger.debug("GET /admin/users/edit/{} - loading edit form for user", id);
        User target = userService.getById(id);
        if (target == null) {
            logger.warn("User with id={} not found. Redirecting to /admin/users", id);
            return "redirect:/admin/users";
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User current = userService.getByUsername(currentUsername);
        boolean editingSelf = current != null && current.getId().equals(target.getId());
        if ("ADMIN".equals(target.getRole()) && !editingSelf) {
            ra.addFlashAttribute("errorMessage", "Cannot change another admin");
            return "redirect:/admin/users";
        }
        model.addAttribute("user", target);
        model.addAttribute("allowAdminOption", editingSelf);
        return "admin/edit-user";
    }

    @PostMapping("/edit")
    public String updateUserRole(@ModelAttribute("user") User user, RedirectAttributes ra) {
        logger.debug("POST /admin/users/edit - updating role for user id={}", user.getId());
        User target = userService.getById(user.getId());
        if (target == null) {
            return "redirect:/admin/users";
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User current = userService.getByUsername(currentUsername);
        boolean editingSelf = current != null && current.getId().equals(target.getId());
        if ("ADMIN".equals(target.getRole()) && !editingSelf) {
            ra.addFlashAttribute("errorMessage", "Cannot change another user");
            return "redirect:/admin/users";
        }
        try {
            userService.updateRole(user.getId(), user.getRole());
            logger.info("Role updated for user id={}", user.getId());
        } catch (Exception e) {
            logger.error("Error updating role for user id={}: {}", user.getId(), e.getMessage());
            ra.addFlashAttribute("errorMessage", "Error with updating new role");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        logger.info("POST /admin/users/delete/{} - deleting user", id);
        User target = userService.getById(id);
        if (target == null) {
            return "redirect:/admin/users";
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User current = userService.getByUsername(currentUsername);
        boolean deletingSelf = current != null && current.getId().equals(id);
        if ("ADMIN".equals(target.getRole()) && !deletingSelf) {
            ra.addFlashAttribute("errorMessage", "Cannot delete another admin");
            return "redirect:/admin/users";
        }
        try {
            userService.deleteUser(target);
            logger.info("User id={} deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error deleting user id={}: {}", id, e.getMessage());
            ra.addFlashAttribute("errorMessage", "Error with deleting");
        }
        return "redirect:/admin/users";
    }
}
