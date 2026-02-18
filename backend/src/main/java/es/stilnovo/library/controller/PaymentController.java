package es.stilnovo.library.controller;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import es.stilnovo.library.model.Product;
import es.stilnovo.library.model.User;
import es.stilnovo.library.repository.ProductRepository;
import es.stilnovo.library.repository.UserRepository;

/**
 * Controller responsible for handling the secure checkout process.
 * Prepares the necessary data for the payment gateway view.
 */
@Controller
public class PaymentController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Renders the payment page for a specific product.
     * Includes security checks to ensure a valid transaction environment.
     * * @param id The ID of the product to purchase.
     * @param principal The security principal of the logged-in buyer.
     * @return The payment view or a redirect if validation fails.
     */
    @GetMapping("/payment-page/{id}")
    public String showPaymentPage(Model model, @PathVariable long id, Principal principal) {

        // 1. Authentication guard
        if (principal == null) {
            return "redirect:/login-page";
        }

        // 2. Fetch target product
        Product product = productRepository.findById(id).orElseThrow();

        // 3. Fetch current logged-in user (the buyer)
        User buyer = userRepository.findByName(principal.getName()).orElseThrow();

        // 4. BUSINESS RULE: Prevent sellers from buying their own items
        if (product.getSeller().getUserId().equals(buyer.getUserId())) {
            return "redirect:/info-product-page/" + id + "?error=self_purchase";
        }

        // 5. STATUS CHECK: Ensure the product is still available for sale
        if (!"active".equalsIgnoreCase(product.getStatus())) {
            return "redirect:/info-product-page/" + id + "?error=not_available";
        }

        // 6. Map attributes for Mustache template rendering
        model.addAttribute("product", product);
        model.addAttribute("user", buyer);

        return "payment-page";
    }
}