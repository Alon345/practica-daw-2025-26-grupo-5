package es.stilnovo.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.stilnovo.library.model.Product;
import es.stilnovo.library.model.Transaction;
import es.stilnovo.library.model.User;
import es.stilnovo.library.repository.ProductRepository;
import es.stilnovo.library.repository.TransactionRepository;
import es.stilnovo.library.repository.UserRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;


    /**
     * Executes the business logic for a purchase.
     * Updates product status and creates a permanent transaction record.
     */
    @Transactional
    public Transaction executePurchase(Product product, User buyer) {
        
        // 1. Business Rule: Product must be 'active'
        if (!"active".equalsIgnoreCase(product.getStatus())) {
            throw new IllegalStateException("Product is no longer available for sale.");
        }

        // 2. Create and configure the Transaction object
        Transaction transaction = new Transaction(
            product.getSeller(),
            buyer,
            product,
            "Completed"
        );

        User seller = product.getSeller();
        Double productPrice = product.getPrice();
        Double sellerBalance = seller.getBalance();
        Double sellerTotalRevenue = seller.getTotalRevenue();

        sellerBalance += productPrice;
        sellerTotalRevenue += productPrice;
        
        seller.setBalance(sellerBalance);
        seller.setTotalRevenue(sellerTotalRevenue);

        userRepository.save(seller);

        // 3. Mark the product as 'Sold' in the database
        product.setStatus("Sold");
        productRepository.save(product);

        // 4. Save the transaction and return it
        return transactionRepository.save(transaction);
    }
}