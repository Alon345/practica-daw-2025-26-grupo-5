package es.stilnovo.library.controller;

import java.security.Principal;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import org.springframework.ui.Model;
import es.stilnovo.library.model.Valoration;
import es.stilnovo.library.model.Transaction;
import es.stilnovo.library.model.User;
import es.stilnovo.library.repository.ValorationRepository;
import es.stilnovo.library.repository.TransactionRepository;
import es.stilnovo.library.repository.UserRepository;

@Controller
public class ValorationController {

    @Autowired
    private ValorationRepository valorationRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/user-valorations-page/{id}")
    public String showValoration(Model model, @PathVariable long id) {

        // 1. Retrieve the user to maintain UI consistency in sidebar and header
        User user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);

        // 2. Filter pending transactions that have not been rated yet
        List<Transaction> allOrders = transactionRepository.findByBuyerUserId(id);
        List<Transaction> pending = new ArrayList<>();

        for (Transaction trans : allOrders) {
            // Ensure the 'rated' transient field is updated before checking
            // Or check existence directly via the repository: valorationRepository.existsByTransaction(trans)
            if (!trans.isRated()) {
                pending.add(trans);
            }
        }
        
        // 3. Add the list and a counter for the UI filter badges
        model.addAttribute("pendingValorations", pending);
        model.addAttribute("pendingCount", pending.size());

        // 4. Fetch all valorations already submitted by this buyer
        model.addAttribute("myValorations", valorationRepository.findByBuyer(user));

        return "user-valorations-page"; 
    }
    
    /**
     * Handles the submission of a new rating.
     * Validates transaction status and updates seller reputation.
     */
    @PostMapping("/valoration/save/{transactionId}")
    public String saveValoration(@PathVariable long transactionId, 
                                @RequestParam int stars, 
                                @RequestParam String comment, 
                                Principal principal) {
        
        // 1. Get the transaction and the current buyer
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow();
        User buyer = userRepository.findByName(principal.getName()).orElseThrow();

        // 2. Create and save the new valoration
        Valoration valoration = new Valoration(transaction, stars, comment);
        valorationRepository.save(valoration);

        // 3. IMPORTANT: Update the transaction status and persist
        transaction.setRated(true); 
        transactionRepository.save(transaction); 

        // 4. Update seller's global rating
        updateSellerRating(transaction.getSeller());

        // 5. Redirect back to the valoration page (the pending list will now be shorter)
        return "redirect:/user-valorations-page/" + buyer.getUserId();
    }

    /**
     * Recalculates and persists the seller's global rating and review count.
     * This ensures the Seller Profile always reflects real-time data.
     */
    private void updateSellerRating(User seller) {
        // 1. Fetch all valorations associated with this seller
        List<Valoration> valorations = valorationRepository.findBySeller(seller);
        
        // 2. Calculate the average score using Java Streams
        double average = valorations.stream()
                .mapToDouble(Valoration::getStars)
                .average()
                .orElse(0.0); // Default to 0.0 if no valorations exist
        
        // 3. Update the seller entity fields
        // Use Math.round or similar if you want to limit decimal places (e.g., 4.5)
        seller.setRating(Math.round(average * 10.0) / 10.0); 
        seller.setNumRatings(valorations.size());
        
        // 4. Persist the updated seller data
        userRepository.save(seller);
    }
}