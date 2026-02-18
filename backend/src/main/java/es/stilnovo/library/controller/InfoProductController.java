package es.stilnovo.library.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import es.stilnovo.library.model.Product;
import es.stilnovo.library.model.User;
import es.stilnovo.library.model.UserInteraction;
import es.stilnovo.library.service.ProductService;
import es.stilnovo.library.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class InfoProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @GetMapping("/info-product-page/{id}")
    public String infoProduct(Model model, @PathVariable long id, HttpServletRequest request) {
        
        // FIX: Ora findById torna Optional, usiamo .orElse(null) per prendere il Product
        Product product = productService.findById(id).orElse(null);
        
        if (product == null) {
             return "error"; 
        }
        
        model.addAttribute("product", product);

        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            String username = principal.getName();
            User currentUser = userService.findByName(username).orElse(null);
            
            if (currentUser != null) {
                productService.saveInteraction(currentUser, product, UserInteraction.InteractionType.VIEW);
            }
        }

        return "info-product-page";
    }
}