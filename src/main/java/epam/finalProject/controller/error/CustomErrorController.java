package epam.finalProject.controller.error;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        int status = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        if (status == 404) {
            return "error/not-found";
        }
        return "error/error";
    }
}
