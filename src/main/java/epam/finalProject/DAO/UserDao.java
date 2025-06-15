package epam.finalProject.DAO;

import epam.finalProject.entity.User;

import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * DAO interface for user data access operations.
 */
public interface UserDao {

    /**
     * Saves a new user in the database.
     *
     * @param user the user to save
     * @return true if saved successfully
     */
    boolean save(User user);

    /**
     * Finds a user by username.
     *
     * @param username the username
     * @return the user object or null
     */
    User findByUsername(String username);

    /**
     * Updates an existing user's password or role.
     *
     * @param user the updated user object
     * @return true if update is successful
     */
    boolean updatePassword(User user);

    /**
     * Shows the list of all users with their info
     *
     * @return Array of users
     */
    long count();

    List<User> findAll(Pageable pageable);

    boolean delete(User user);

    User findById(Long id);

    boolean updateRole(Long id, String newRole);
}
