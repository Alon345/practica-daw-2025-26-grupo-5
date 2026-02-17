package es.stilnovo.library.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.stilnovo.library.model.Product;
import es.stilnovo.library.model.User;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Spring generates the SQL automatically: SELECT * FROM Product WHERE user_id = ?
    List<Product> findBySeller(User seller);

    // Efficiently counts only the number of products associated with this seller
    long countBySeller(User seller);

    // Finds products whose name contains the query string, ignoring case
    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryContainingIgnoreCase(String category);

    List<Product> findTop8ByOrderByIdDesc();
    
    @Query(value = """
        SELECT p.* FROM product p
        JOIN (
            SELECT 
                prod.category as cat_name, 
                SUM(CASE 
                    WHEN ui.type = 'BUY' THEN 5 
                    WHEN ui.type = 'LIKE' THEN 3 
                    WHEN ui.type = 'VIEW' THEN 1 
                    ELSE 0 END) as score
            FROM user_interactions ui
            JOIN product prod ON ui.product_id = prod.id
            WHERE ui.user_id = :userId
            GROUP BY prod.category
        ) as prefs ON p.category = prefs.cat_name
        WHERE p.seller_user_id != :userId 
        ORDER BY prefs.score DESC, p.id DESC
        LIMIT 8
        """, nativeQuery = true)
    List<Product> findRecommendedProducts(@Param("userId") Long userId);
}