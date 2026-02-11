package es.stilnovo.library.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import es.stilnovo.library.repository.UserRepository;
import es.stilnovo.library.service.ProductService;

@Controller
public class MainController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository; // Essential to find the userId

    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();

        if (principal != null) {
            // Search user. If really exists in actual DB...
            userRepository.findByName(principal.getName()).ifPresentOrElse(user -> {
                model.addAttribute("logged", true);
                model.addAttribute("username", user.getName());
                model.addAttribute("userId", user.getUserId());
                model.addAttribute("admin", request.isUserInRole("ADMIN"));
            }, () -> {
            // ...if not exists (fantasm user), we treat it like a no logged user
                model.addAttribute("logged", false);
            });
        } else {
            model.addAttribute("logged", false);
        }
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("products", productService.findAll());
        return "index";
    }
}