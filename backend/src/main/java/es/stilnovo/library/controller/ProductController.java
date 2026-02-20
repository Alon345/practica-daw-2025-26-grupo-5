package es.stilnovo.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import java.util.List;

import es.stilnovo.library.model.Product;
import es.stilnovo.library.service.ProductService; 

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/load-more-products")
    public String loadMore(@RequestParam int page, Model model) {
        List<Product> moreProducts = productService.getProductsByPage(page, 4); // 4 products per page
        model.addAttribute("products", moreProducts);
        
        // Retorna solo el fragmento, no la página completa
        return "product_items"; 
    }
}