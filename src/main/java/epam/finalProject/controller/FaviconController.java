package epam.finalProject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/*
DO NOT DELETE THIS
 */
@Controller
public class FaviconController {
    @RequestMapping("favicon.ico")
    @ResponseBody
    public void returnNoFavicon() {
    }
}
