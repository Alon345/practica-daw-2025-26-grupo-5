package es.stilnovo.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.stilnovo.library.model.Transaction;
import java.util.List;

/**
 * Repository for Transaction entities.
 * Provides methods to retrieve sales and purchases for specific users.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Retrieves all transactions where the user was the buyer
    List<Transaction> findByBuyerUserId(Long buyerId);
    
    // Retrieves all transactions where the user was the seller
    List<Transaction> findBySellerUserId(Long sellerId);
}