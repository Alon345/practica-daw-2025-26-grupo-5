package es.stilnovo.library.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.stilnovo.library.model.Product;
import es.stilnovo.library.model.User;
import es.stilnovo.library.repository.UserRepository;

/**
 * Service to offload logic from the MainController.
 * Handles product searching and user context retrieval.
 */
@Service
public class MainService {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Executes product search based on query or category.
     */
    public List<Product> searchProducts(String query, String category) {
        if (category != null && !category.isEmpty()) {
            return productService.findByQueryCategory(category);
        }
        return productService.findByQuery(query);
    }

    /**
     * Retrieves the full user profile safely.
     */
    public User getUserContext(String username) {
        if (username == null) return null;
        return userRepository.findByName(username).orElse(null);
    }

    public List<Product> getInitialProducts() {
        return productService.getProductsByPage(0, 4); // First page with 4 products
    }
}