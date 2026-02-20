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

import es.stilnovo.library.service.MainService;

@Controller
public class MainController {

    @Autowired
    private MainService mainService;

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) String category,
                        Principal principal) {

        // 1. Get typed data from Service (NO casting needed = NO errors)
        List<Product> initialProducts = mainService.getInitialProducts();
        User user = mainService.getUserContext(principal != null ? principal.getName() : null);

        // 2. Populate Model with all needed data for the view (NO logic here = NO errors)
        boolean logged = (user != null);
        boolean isAdmin = logged && user.getRoles().contains("ROLE_ADMIN");

        model.addAttribute("products", initialProducts);
        model.addAttribute("user", user);
        model.addAttribute("logged", logged);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("query", (query != null) ? query : (category != null ? category : ""));
        model.addAttribute("searching", query != null || category != null);

        /*// 2. Product Search Logic
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

            // Redirect if unique result
            if (products.size() == 1) {
                return "redirect:/info-product-page/" + products.get(0).getId();
            }

        } else {
            // Recommendation Algorithm
            products = productService.getRecommendations(currentUser);
        }

        // 3. Favorites Logic
            if (currentUser != null && products != null) {
                List<Product> userFavs = currentUser.getFavoriteProducts(); 
                for (Product p : products) {
                    boolean isFav = userFavs.stream().anyMatch(fav -> fav.getId().equals(p.getId())); 
                    p.setFavorite(isFav); 
                }
            }

        model.addAttribute("searching", isSearching);
        model.addAttribute("products", products);*/

        // 3. Navigation Logic
        if (initialProducts.size() == 1 && (query != null || category != null)) {
            return "redirect:/info-product-page/" + initialProducts.get(0).getId();
        }

        return "index";
    }
}