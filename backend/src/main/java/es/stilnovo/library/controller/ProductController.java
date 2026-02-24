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
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/load-more-products")
    public String loadMore(@RequestParam int page, Model model) {
        int pageSize = 10;

        List<Product> moreProducts = productService.getProductsByPage(page, pageSize); // 10 products per page

        boolean isLast = productService.getProductsByPage(page + 1, pageSize).isEmpty();

        model.addAttribute("products", moreProducts);
        model.addAttribute("isLast", isLast);
        
        // Return only the fragment, not the full page
        return "product_items"; 
    }
}