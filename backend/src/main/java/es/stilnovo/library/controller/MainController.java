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
import es.stilnovo.library.repository.UserRepository;
import es.stilnovo.library.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MainController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String index(Model model, 
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) String category,
                        HttpServletRequest request) { // Added request to handle session/principal
    
        List<Product> products;

        // 1. Logic: Decide which search/filter method to use
        if (category != null && !category.isEmpty()) {
            products = productService.findByQueryCategory(category);
            model.addAttribute("query", category);
        } else {
            products = productService.findByQuery(query);
            model.addAttribute("query", (query != null) ? query : "");
        }

        // 2. Favorite Logic: Mark products favorited by the current user [cite: 241]
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            // Get the logged-in user and their favorites list
            User user = userRepository.findByName(principal.getName()).orElseThrow();
            List<Product> userFavs = user.getFavoriteProducts();
            
            // Set the dynamic flag for Mustache templates
            for (Product p : products) {
                p.setFavorite(userFavs.contains(p));
            }
        }

        // 3. Redirect if a unique result is found during search [cite: 14]
        if (products.size() == 1 && (query != null || category != null)) {
            return "redirect:/info-product-page/" + products.get(0).getId();
        }

        // 4. UI state: Set flag to hide/show Hero section
        boolean isSearching = (query != null && !query.isEmpty()) || (category != null && !category.isEmpty());
        model.addAttribute("searching", isSearching);
        model.addAttribute("products", products);

        return "index";
    }
}