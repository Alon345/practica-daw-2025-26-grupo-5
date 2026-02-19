package es.stilnovo.library.model;

import java.util.List;

import jakarta.persistence.*;

@Entity(name = "ProductTable")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String category;
    private double price;
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status; // active, inactive
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Image image;

    /* To be implemented 
    //1 Product = N Images
    @OneToMany(cascade  = CascadeType.ALL)
    private List<Image> images;*/


    @ManyToOne
    private User seller; // product owner

    // --- ADDED: Transient field for UI Logic (Uncommented) ---
    // Transient means this field is NOT saved to the database
    @Transient
    private boolean favorite; // Temporary flag for the view

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public Product() {}

    public Product(String name, String category, double price, String description, String status, User seller, String location) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.status = status;
        this.seller = seller;
        this.location = location;
    }


    // --- GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Image getImage() {return image;}
    public void setImage(Image image){this.image = image;}

    /* To be implemented 
    public List<Image> getImages() {return images;}
    public void setImages(List<Image> images){this.images = images;} */

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    /**
     * Helper method for UI logic. 
     * Mustache interprets this as the 'isActive' boolean property.
     */
    public boolean isActive() {
        return "Active".equalsIgnoreCase(this.status);
    }

}   