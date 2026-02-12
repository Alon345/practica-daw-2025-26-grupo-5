package es.stilnovo.library.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.stilnovo.library.model.Product;
import es.stilnovo.library.service.ProductService;

@Controller
public class MainController {

    @Autowired
    private ProductService productService;

    // Added @RequestParam to capture the search input from the HTML form
    @GetMapping("/")
    public String index(Model model, 
                    @RequestParam(required = false) String query,
                    @RequestParam(required = false) String category) { // New parameter
    
        List<Product> products;

        // 1. Logic: Decide which service method to use
        if (category != null && !category.isEmpty()) {
            products = productService.findByQueryCategory(category);
            model.addAttribute("query", category); // To show what category we are in
        } else {
            products = productService.findByQuery(query);
            model.addAttribute("query", (query != null) ? query : "");
        }

        // 2. Reuse your existing logic for single product redirect
        if (products.size() == 1 && (query != null || category != null)) {
            return "redirect:/info-product-page/" + products.get(0).getId();
        }

      // 3. Set the 'searching' flag to hide the Hero section
        boolean isSearching = (query != null && !query.isEmpty()) || (category != null && !category.isEmpty());
        model.addAttribute("searching", isSearching);
        model.addAttribute("products", products);

        return "index";
    }
}