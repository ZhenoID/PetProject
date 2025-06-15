package epam.finalProject.controller;

import epam.finalProject.service.UserService;
import epam.finalProject.service.UserServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private final UserService userService = new UserServiceImpl();

    /**
     * Entrance point
     *
     * @return
     */
    @GetMapping("/")
    public String index() {

        return "home";
    }
}
