package es.stilnovo.library.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_interactions")
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id") 
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    private InteractionType type; // VIEW, CLICK, BUY

    private LocalDateTime timestamp;

    public UserInteraction() {}

    public UserInteraction(User user, Product product, InteractionType type) {
        this.user = user;
        this.product = product;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public User getUser() { return user; }
    public Product getProduct() { return product; }
    public InteractionType getType() { return type; }
    
    // Enum for interaction types
    public enum InteractionType {
        VIEW, LIKE, BUY
    }
}