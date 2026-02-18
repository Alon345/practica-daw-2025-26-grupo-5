package es.stilnovo.library.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Model Imports
import es.stilnovo.library.model.Valoration;
import es.stilnovo.library.model.User;
import es.stilnovo.library.model.Transaction;

// Repository Imports
import es.stilnovo.library.repository.ValorationRepository;
import es.stilnovo.library.repository.UserRepository;
import es.stilnovo.library.repository.TransactionRepository;

@Service
public class ValorationService {

    @Autowired
    private ValorationRepository valorationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public void saveAndUpdateSellerRating(long transactionId, int stars, String comment, User buyer) {
        // 1. Logic to find and validate the transaction
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow();
        
        // 2. Create and save the new valoration
        Valoration valoration = new Valoration(transaction, stars, comment);
        valorationRepository.save(valoration);

        // 3. Update Seller Statistics
        User seller = transaction.getSeller();
        updateSellerStats(seller);
    }

    private void updateSellerStats(User seller) {
        List<Valoration> valorations = valorationRepository.findBySeller(seller);
        double average = valorations.stream()
                .mapToDouble(Valoration::getStars)
                .average()
                .orElse(0.0);
        
        seller.setRating(average);
        seller.setNumRatings(valorations.size());
        userRepository.save(seller);
    }
}