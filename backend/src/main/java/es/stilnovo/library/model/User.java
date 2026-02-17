package es.stilnovo.library.model;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;

@Entity(name = "UserTable")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long userId;

	private String name;
	private String userDescription;

	private String cardNumber;
	private String cardExpiringDate;
	private String cardCvv;

	private Double balance;
	private Double totalRevenue;

	private String encodedPassword;

	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> roles;

	@Lob
	private Blob profileImage; 	

	private Double rating;
	private Integer numRatings;
	
	@Column(unique = true, nullable = false) 
	private String email;

	// Synchronizes P2P data: propagates deletions to products and removes orphaned entries
	@OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Product> products = new ArrayList<>();

	public User() {
	}

	public User(String name, String encodedPassword, String email, Blob profileImage, Double rating, String cardNumber, String cardExpiringDate, String cardCvv, Integer numRatings,Double balance, Double totalRevenue, String userDescription,String... roles) {
		this.name = name;
		this.encodedPassword = encodedPassword;
		this.email = email;
		this.profileImage = profileImage;
		this.rating = rating;
		this.roles = List.of(roles);
		this.cardNumber = cardNumber;
		this.cardExpiringDate = cardExpiringDate;
		this.cardCvv = cardCvv;
		this.balance = balance;
		this.numRatings = numRatings;
		this.totalRevenue = totalRevenue;
		this.userDescription = userDescription;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	
	public String getEncodedPassword() {
		return encodedPassword;
	}

	public void setEncodedPassword(String encodedPassword) {
		this.encodedPassword = encodedPassword;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public Blob getProfileImage() { 
		return profileImage; 
	}

	public void setProfileImage(Blob profileImage) { 
		this.profileImage = profileImage; 
	}

	public Double getRating() { 
		return rating; 
	}

	public void setRating(Double rating) { 
		this.rating = rating; 
	}

	public Integer getNumRatings() { 
		return numRatings; 
	}

	public void setNumRatings(Integer numRatings) { 
		this.numRatings = numRatings; 
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getCardExpiringDate() {
		return cardExpiringDate;
	}

	public void setCardExpiringDate(String cardExpiringDate) {
		this.cardExpiringDate = cardExpiringDate;
	}

	public String getCardCvv() {
		return cardCvv;
	}

	public void setCardCvv(String cardCvv) {
		this.cardCvv = cardCvv;
	}


	/*//Favorite products
	@ManyToMany
	private List<Product> favoriteProducts = new ArrayList<>();

	public List<Product> getFavoriteProducts() {
    	return favoriteProducts;
	}

	public void addFavorite(Product product) {
		//if array not contain product then add it
    	if (!this.favoriteProducts.contains(product)) {
        	this.favoriteProducts.add(product);
    	}
	}*/

	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public Double getTotalRevenue() {
		return totalRevenue;
	}
	public void setTotalRevenue(Double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	public String getUserDescription() {
		return userDescription;
	}
	public void setUserDescription(String userDescription) {
		this.userDescription = userDescription;
	}


	public Long getUserId() {
        return userId;
    }
}