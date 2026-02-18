package es.stilnovo.library.model;

import jakarta.persistence.*;

/**
 * Entity representing user feedback after a completed transaction. [cite: 93]
 * Essential for the recommendation algorithm and seller reputation. [cite: 60, 65]
 */
@Entity
@Table(name = "ValorationTable")
public class Valoration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int stars; // Numeric score (1 to 5) 

    @Column(columnDefinition = "TEXT")
    private String comment; // Subjective review 

    @OneToOne
    private Transaction transaction; // Strictly linked to a completed transaction

    @ManyToOne
    private User seller; // The user receiving the rating

    @ManyToOne
    private User buyer; // The user giving the rating

    public Valoration() {}

    public Valoration(Transaction transaction, int stars, String comment) {
        this.transaction = transaction;
        this.stars = stars;
        this.comment = comment;
        this.seller = transaction.getSeller();
        this.buyer = transaction.getBuyer();
    }

    // --- GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Transaction getTransaction() { return transaction; }
    public User getSeller() { return seller; }
    public User getBuyer() { return buyer; }
}