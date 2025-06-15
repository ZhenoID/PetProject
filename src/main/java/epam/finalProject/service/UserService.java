package epam.finalProject.service;

import epam.finalProject.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for user-related business logic.
 */
public interface UserService {

    /**
     * Registers a new user.
     *
     * @param user the user to register
     * @return true if registration is successful, false if username is taken
     */
    boolean register(User user);

    /**
     * Authenticates a user based on username and password.
     *
     * @param username the username
     * @param password the plain text password
     * @return true if credentials are valid, false otherwise
     */

    /**
     * Retrieves a user by username.
     *
     * @param username the username to search
     * @return the user object, or null if not found
     */
    User getByUsername(String username);

    /**
     * Updates user information (e.g., password).
     *
     * @param user the updated user object
     * @return true if update is successful, false otherwise
     */
    boolean updatePassword(User user);

    boolean updateRole(Long userId, String newRole);

    /**
     * Shows all users
     *
     * @return list of users
     */
    Page<User> findAll(Pageable pageable);

    boolean deleteUser(User user);

    User getById(Long id);

    boolean authenticate(String username, String password);
}
