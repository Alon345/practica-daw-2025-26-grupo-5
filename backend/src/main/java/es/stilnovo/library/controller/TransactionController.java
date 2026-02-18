package es.stilnovo.library.controller;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import es.stilnovo.library.model.Product;
import es.stilnovo.library.model.User;
import es.stilnovo.library.repository.ProductRepository;
import es.stilnovo.library.repository.UserRepository;
import es.stilnovo.library.service.MailService;
import es.stilnovo.library.service.TransactionService;

@Controller
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    /**
     * Handles the POST request from the payment gateway.
     * Triggers the service logic and notifies the buyer via email.
     */
    @PostMapping("/api/v1/transactions/confirm/{productId}")
    public String confirmPayment(@PathVariable long productId, Principal principal) {
        
        // 1. Load context data
        User buyer = userRepository.findByName(principal.getName()).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        try {
            // 2. Delegate business logic to the service layer
            transactionService.executePurchase(product, buyer);

            // 3. Send automated confirmation email
            String body = "<h1>Purchase Successful!</h1><p>You have bought: " + product.getName() + "</p>";
            mailService.sendHtml(buyer.getEmail(), "Stilnovo: Purchase Confirmation", body);

            // 4. Success redirect to Sales & Orders page
            return "redirect:/sales-and-orders-page/" + buyer.getUserId() + "?productId=" + productId;

        } catch (IllegalStateException e) {
            // Handle cases where the item was sold mid-process
            return "redirect:/info-product-page/" + productId + "?error=not_available";
        }
    }
}