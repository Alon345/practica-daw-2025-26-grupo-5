package es.stilnovo.library.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.stilnovo.library.model.Product;
import es.stilnovo.library.model.User;
import es.stilnovo.library.model.UserInteraction;
import es.stilnovo.library.repository.ProductRepository;
import es.stilnovo.library.repository.UserInteractionRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserInteractionRepository userInteractionRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // --- FIX CRITICO: Ritorna Optional per non rompere gli altri Controller ---
    public Optional<Product> findById(long id) {
        return productRepository.findById(id);
    }
    // ------------------------------------------------------------------------

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(long id) {
        productRepository.deleteById(id);
    }

    // Search methods
    public List<Product> findByQuery(String query) {
        if (query == null || query.isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.findByNameContainingIgnoreCase(query);
    }

    public List<Product> findByQueryCategory(String category) {
        return productRepository.findByCategoryContainingIgnoreCase(category);
    }

    public List<Product> findBySeller(User seller) {
        return productRepository.findBySeller(seller);
    }

    // --- FIX: Aggiunto metodo mancante per UserWebController ---
    public long getProductCount(User seller) {
        return productRepository.countBySeller(seller);
    }
    // ----------------------------------------------------------

    // ALGORITHM METHODS
    public List<Product> getRecommendations(User user) {
        if (user == null) {
            return productRepository.findTop8ByOrderByIdDesc();
        }

        List<Product> recommendations = productRepository.findRecommendedProducts(user.getUserId());

        if (recommendations.isEmpty()) {
            return productRepository.findTop8ByOrderByIdDesc();
        }

        return recommendations;
    }

    public void saveInteraction(User user, Product product, UserInteraction.InteractionType type) {
        if (user != null && product != null) {
            UserInteraction interaction = new UserInteraction(user, product, type);
            userInteractionRepository.save(interaction);
        }
    }
}