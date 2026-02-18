package es.stilnovo.library.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "TransactionTable")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long transactionId;

    @ManyToOne
    private User seller;

    @ManyToOne
    private User buyer;

    @OneToOne
    private Product product;

    private double finalPrice;
    private LocalDateTime createdAt;
    private String transactionStatus; 

    // Rating integration
    // We use @Transient so it's NOT saved in the database, 
    // it's only used for the logic of the view
    private boolean rated;
    private Integer stars; // 1 to 5

    public Transaction() {}

    public Transaction(User seller, User buyer, Product product, String status) {
        this.seller = seller;
        this.buyer = buyer;
        this.product = product;
        this.finalPrice = product.getPrice();
        this.transactionStatus = status;
        this.createdAt = LocalDateTime.now();
        this.rated = false;
    }

    // --- GETTERS AND SETTERS ---
    
    public Long getTransactionId() { return transactionId; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public double getFinalPrice() { return finalPrice; }
    public void setFinalPrice(double finalPrice) { this.finalPrice = finalPrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }

    public boolean isRated() { return rated; }
    public void setRated(boolean rated) { this.rated = rated; }

    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
}