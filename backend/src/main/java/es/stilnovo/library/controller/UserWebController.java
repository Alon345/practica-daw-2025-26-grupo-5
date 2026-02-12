package es.stilnovo.library.controller;

import java.io.IOException;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.hibernate.engine.jdbc.proxy.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import es.stilnovo.library.model.Image;
import es.stilnovo.library.model.Product;
import es.stilnovo.library.model.User;
import es.stilnovo.library.repository.ImageRepository;
import es.stilnovo.library.repository.ProductRepository;
import es.stilnovo.library.repository.UserRepository;
import es.stilnovo.library.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UserWebController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductService productService;

    @Autowired
    private ImageRepository imageRepository;

    // ... other methods like /profile or /edit-profile ...

    /**
     * Endpoint to retrieve a specific user's profile photo from the database.
     * It fetches the Blob content and returns it as a streaming image resource.
     */
    @GetMapping("/user/{id}/profile-photo")
    public ResponseEntity<Object> getProfilePhoto(@PathVariable long id) throws SQLException {
        
        // Find the user in the database; throws an exception if the ID doesn't exist
        User user = userRepository.findById(id).orElseThrow();
        
        // Check if the user has a profile image (stored as a Blob)
        if (user.getProfileImage() != null) {
            Resource file = new InputStreamResource(user.getProfileImage().getBinaryStream());
            
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Adjust the media type if necessary
                .body(file);
        }
        
        // Return a 404 Not Found if the user exists but has no image
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/seller-profile/{id}")
    public String showSellerProfile(Model model, @PathVariable long id) {
        // 1. Find the seller by ID. If not found, Spring will show the error page we created.
        User seller = userRepository.findById(id).orElseThrow();
    
        // 2. Add the seller object to the model
        model.addAttribute("seller", seller);

        // 3. Fetch ONLY the products belonging to this seller
        List<Product> sellerProducts = productRepository.findBySeller(seller);
        model.addAttribute("sellerProducts", sellerProducts);
    
        // 4. Logic for stars (optional: you could pass a list of booleans for the star icons)
        model.addAttribute("fullStars", Math.floor(seller.getRating()));
        
        // 5. Num of products
        long itemsCount = productService.getProductCount(seller);
        model.addAttribute("itemsCount", itemsCount);

        return "seller-profile-page";
    }


    @GetMapping("/user-page/{id}")
    public String showUserPage(Model model, @PathVariable long id, HttpServletRequest request) {
    
        // 1. Buscamos al usuario por su ID
        User user = userRepository.findById(id).orElseThrow();
    
        // 2. Pasamos los datos del usuario a la plantilla user-page.html
        model.addAttribute("user", user);
    
        // 3. Comprobamos si el que mira la página es el dueño del perfil
        Principal principal = request.getUserPrincipal();
        if (principal != null && principal.getName().equals(user.getName())) {
        model.addAttribute("isOwner", true);
        }

        return "user-page"; 
    }


    //user-products-page.html
    @GetMapping("/user-products-page/{id}")
    public String userProducts(Model model, @PathVariable long id, HttpServletRequest request) {

        // 1. Fetch the user who owns the profile from the database
        User user = userRepository.findById(id).orElseThrow();

        // 2. SECURITY CHECK: Verify if the currently logged-in user is the owner of this inventory
        // We use the request's principal name (username/email) to validate access
        if (request.getUserPrincipal() == null || !request.getUserPrincipal().getName().equals(user.getName())) {
            // Unauthorized access: redirect to an error or access-denied page
            return "redirect:/error"; 
        }

        // 3. Retrieve the products. We must use 'findBySeller' because that is the field name 
        // defined in the Product entity class
        List<Product> userProducts = productRepository.findBySeller(user);
    
        // 4. Populate the model for the Mustache template
        model.addAttribute("userProducts", userProducts);
        model.addAttribute("user", user); 
        model.addAttribute("itemsCount", userProducts.size());

        return "user-products-page";
    }


    // GET method to display the edit form with existing data
    @GetMapping("/edit-product-page/{id}")
    public String showEditForm(Model model, @PathVariable long id, HttpServletRequest request) {
    
        // 1. Find the product by its ID
        Product product = productRepository.findById(id).orElseThrow();

        // 2. SECURITY CHECK: Ensure the logged-in user is the owner (the seller)
        if (request.getUserPrincipal() == null || !request.getUserPrincipal().getName().equals(product.getSeller().getName())) {
            return "redirect:/error-403";
        }

        // 3. Add the product to the model so the form fields can be pre-filled
        model.addAttribute("product", product);
    
        return "edit-product-page"; 
    }


    // POST method to handle the form submission and update the DB
    @PostMapping("/edit-product/{id}")
    public String updateProduct(@PathVariable long id, Product updatedProduct, HttpServletRequest request) {
    
        Product existingProduct = productRepository.findById(id).orElseThrow();

        // 1. SECURITY CHECK (Mandatory again for the POST request)
        if (request.getUserPrincipal() == null || !request.getUserPrincipal().getName().equals(existingProduct.getSeller().getName())) {
            return "redirect:/error-403";
        }

        // 2. Update fields
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setDescription(updatedProduct.getDescription());
        // ... update other fields ...

        // 3. Save to database
        productRepository.save(existingProduct);

        // 4. Redirect back to the inventory page
        return "redirect:/user-products-page/" + existingProduct.getSeller().getUserId();
    } 
    
    
    // GET method to display the add form 
    @GetMapping("/add-product-page")
    public String showAddForm(Model model, HttpServletRequest request) {
    
        return "add-product-page"; 
    }

    @PostMapping("/add-product")
    public String newProduct(Model model, 
                            @RequestParam("productPhoto") MultipartFile[] productPhotos, 
                            @RequestParam String productName,
                            @RequestParam String category,
                            @RequestParam String description,
                            @RequestParam double price,
                            @RequestParam String location,
                            @RequestParam String status,
                            HttpServletRequest request) throws IOException {

        // 1. Filter empty files and validate count (1 to 4)
        List<MultipartFile> validPhotos = Arrays.stream(productPhotos)
                                                .filter(f -> !f.isEmpty())
                                                .toList();
        
        if (validPhotos.size() < 1 || validPhotos.size() > 4) {
            // Add error message
            model.addAttribute("error", "You must upload between 1 and 4 photos.");
            
            // MANTAIN FIELDS: Add the values back to the model
            model.addAttribute("productName", productName);
            model.addAttribute("category", category);
            model.addAttribute("price", price);
            model.addAttribute("location", location);
            model.addAttribute("description", description);
            model.addAttribute("status", status);
            
            return "add-product-page"; // Return view name, NOT redirect
        }

        // 2. Identify authenticated seller
        Principal principal = request.getUserPrincipal();
        User seller = userRepository.findByName(principal.getName()).orElseThrow();

        // 3. Create product
        Product newProduct = new Product(productName, category, price, description, status, seller, location);

        // 4. Link images to product
        for (MultipartFile photo : validPhotos) {
            Image img = new Image();
            img.setImageFile(BlobProxy.generateProxy(photo.getInputStream(), photo.getSize()));
            newProduct.getImages().add(img); 
        }

        productRepository.save(newProduct);

        return "redirect:/user-products-page/" + seller.getUserId();
    }

    // Method to delete a product from the database
    @PostMapping("/delete-product/{id}")
    public String deleteProduct(@PathVariable long id, HttpServletRequest request) {
    
        // 1. Get current logged-in user
        Principal principal = request.getUserPrincipal();
        User user = userRepository.findByName(principal.getName()).orElseThrow();
    
        // 2. Find the product in the DB
        Product product = productRepository.findById(id).orElseThrow();
    
        // 3. SECURITY: Verify ownership before deleting
        if (product.getSeller().getUserId() == user.getUserId()) {
            // This will also delete associated images due to CascadeType.ALL
            productRepository.delete(product);
        }
    
        // 4. Redirect back to the inventory page
        return "redirect:/user-products-page/" + user.getUserId();
    }
    
    
}