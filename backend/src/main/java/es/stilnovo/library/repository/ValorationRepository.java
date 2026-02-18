package es.stilnovo.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.stilnovo.library.model.Valoration;
import es.stilnovo.library.model.User;
import java.util.List;

/**
 * Repository for Valoration entities.
 * Critical for calculating seller reputation and fueling the recommendation algorithm.
 */
public interface ValorationRepository extends JpaRepository<Valoration, Long> {

    // Fetch all ratings received by a specific seller
    List<Valoration> findBySeller(User seller);

    // Fetch all ratings given by a specific buyer
    List<Valoration> findByBuyer(User buyer);
    
    // Check if a transaction has already been rated to prevent duplicates
    boolean existsByTransactionTransactionId(Long transactionId);
}