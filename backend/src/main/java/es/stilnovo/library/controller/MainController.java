package es.stilnovo.library.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.stilnovo.library.model.Product;
import es.stilnovo.library.model.User;
import es.stilnovo.library.service.ProductService;
import es.stilnovo.library.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MainController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index(Model model, 
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) String category,
                        HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        User currentUser = null;
        
        if (principal != null) {
            String username = principal.getName();
            // Ora funziona perché abbiamo aggiunto findByName in UserService
            currentUser = userService.findByName(username).orElse(null);
            
            if (currentUser != null) {
                model.addAttribute("logged", true);
                model.addAttribute("username", currentUser.getName());
                model.addAttribute("userId", currentUser.getUserId());
                model.addAttribute("admin", request.isUserInRole("ADMIN"));
            }
        } else {
            model.addAttribute("logged", false);
        }

        List<Product> products;
        boolean isSearching = (query != null && !query.isEmpty()) || (category != null && !category.isEmpty());

        if (isSearching) {
            if (category != null && !category.isEmpty()) {
                products = productService.findByQueryCategory(category);
                model.addAttribute("query", category);
            } else {
                products = productService.findByQuery(query);
                model.addAttribute("query", query);
            }

            if (products.size() == 1) {
                return "redirect:/info-product-page/" + products.get(0).getId();
            }

        } else {
            products = productService.getRecommendations(currentUser);
        }

        if (currentUser != null) {
            List<Product> userFavs = currentUser.getFavoriteProducts();
            if (products != null) {
                for (Product p : products) {
                    // FIX: p.getId() invece di p.getUserId() (Product non ha getUserId)
                    // FIX: fav.getId() invece di fav.getUserId() (fav è un Product)
                    boolean isFav = userFavs.stream().anyMatch(fav -> fav.getId().equals(p.getId())); 
                    p.setFavorite(isFav); 
                }
            }
        }

        model.addAttribute("searching", isSearching);
        model.addAttribute("products", products);

        return "index";
    }
}