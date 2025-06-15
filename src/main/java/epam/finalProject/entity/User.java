package epam.finalProject.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class User {
    private Long id;
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "{user.username.pattern}")
    @NotBlank(message = "{user.username.notblank}")
    @Size(min = 3, max = 20, message = "{user.username.size}")
    private String username;

    @NotBlank(message = "{user.password.notblank}")
    @Size(min = 6, message = "{user.password.size}")
    private String password;

    private String role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}