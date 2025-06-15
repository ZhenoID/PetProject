package epam.finalProject.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Error: " + ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(SQLException.class)
    public String handleSQLException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Error on our side: " + ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/not-found";
    }

}
