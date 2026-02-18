package es.stilnovo.library.controller;

import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList; 
import java.util.Arrays;
import java.util.List;

import org.hibernate.engine.jdbc.proxy.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.stilnovo.library.model.Image;
import es.stilnovo.library.model.Product;
import es.stilnovo.library.model.Transaction;
import es.stilnovo.library.model.User;
import es.stilnovo.library.model.Valoration;
import es.stilnovo.library.repository.ProductRepository;
import es.stilnovo.library.repository.TransactionRepository;
import es.stilnovo.library.repository.UserRepository;
import es.stilnovo.library.repository.ValorationRepository;
import es.stilnovo.library.service.ProductService;
import es.stilnovo.library.service.UserService;
import jakarta.servlet.ServletException;
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
    private TransactionRepository transactionRepository;

    @Autowired
    private ValorationRepository valorationRepository;

    @Autowired
    private UserService userService;

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
        
        List<Valoration> valorationList = valorationRepository.findBySeller(seller);
        model.addAttribute("sellerValorations", valorationList); 
    
        // 2. Add the seller object to the model
        model.addAttribute("seller", seller);

        // 3. Fetch ONLY the products belonging to this seller
        List<Product> sellerProducts = productRepository.findBySeller(seller);
        model.addAttribute("sellerProducts", sellerProducts);
    
        // 4. Logic for stars 
        model.addAttribute("fullStars", Math.floor(seller.getRating()));
        
        // 5. Num of products
        long itemsCount = productService.getProductCount(seller);
        model.addAttribute("itemsCount", itemsCount);

        return "seller-profile-page";
    }


    @GetMapping("/user-page/{id}")
    public String showUserPage(Model model, @PathVariable long id, HttpServletRequest request) {
    
        // 1. Search user by ID
        User user = userRepository.findById(id).orElseThrow();
    
        // 2. Give all the user object to the model
        model.addAttribute("user", user);
    
        // 3. Check if current user is owner 
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

    @GetMapping("/sales-and-orders-page/{id}")
    public String showSalesAndOrdersPage(Model model, @PathVariable long id,
                                         @RequestParam(required = false) Long productId,
                                         HttpServletRequest request) {

        // 1. Context and Security
        User user = userRepository.findById(id).orElseThrow();
        if (request.getUserPrincipal() == null || !request.getUserPrincipal().getName().equals(user.getName())) {
            return "redirect:/error";
        }

        // 2. Fetch Data using the new Power: Transactions
        List<Transaction> sales = transactionRepository.findBySellerUserId(user.getUserId());
        List<Transaction> orders = transactionRepository.findByBuyerUserId(user.getUserId());

        // Add a check for each order
        for(Transaction t: orders){
            // Check if a valoration already exists for this transaction
            t.setRated(valorationRepository.existsByTransactionTransactionId(t.getTransactionId()));        
        }

        // 3. Logic for the "Detail View" (Selected Sale)
        Transaction selectedSale = null;
        if (productId != null) {
            selectedSale = sales.stream()
                .filter(t -> t.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
        }
        
        // Default: Show the most recent sale if none is clicked
        if (selectedSale == null && !sales.isEmpty()) {
            selectedSale = sales.get(0);
        }

        // 4. FIXING THE 500 ERROR: Always provide a default for Mustache
        String address = (selectedSale != null) ? selectedSale.getProduct().getLocation() : "No shipping data available";

        // 5. Populate Model
        model.addAttribute("user", user);
        model.addAttribute("sales", sales);
        model.addAttribute("orders", orders); // you can see what you bought!
        model.addAttribute("selectedTransaction", selectedSale);
        model.addAttribute("selectedProductAddress", address);
        model.addAttribute("hasSales", !sales.isEmpty());
        model.addAttribute("hasOrders", !orders.isEmpty());

        return "sales-and-orders-page";
    }


    @GetMapping("/help-center-page/{id}")
    public String showHelpCenterPage(Model model, @PathVariable long id, HttpServletRequest request) {

        User user = userRepository.findById(id).orElseThrow();

        if (request.getUserPrincipal() == null || !request.getUserPrincipal().getName().equals(user.getName())) {
            return "redirect:/error";
        }

        model.addAttribute("user", user);

        return "help-center-page";
    }


    // GET method to display the edit form with existing data
    @GetMapping("/edit-product-page/{id}")
    public String showEditForm(Model model, @PathVariable long id, HttpServletRequest request) {
    
        // 1. Find the product by its ID
        Product product = productRepository.findById(id).orElseThrow();

        // 2. SECURITY CHECK: Ensure the logged-in user is the owner (the seller)
        if (request.getUserPrincipal() == null || !request.getUserPrincipal().getName().equals(product.getSeller().getName())) {
            return "redirect:/error";
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
        // AGGIUNTA: Inizializza la lista se null per evitare NullPointerException
        if (newProduct.getImages() == null) {
            newProduct.setImages(new ArrayList<>());
        }

        for (MultipartFile photo : validPhotos) {
            Image img = new Image();
            img.setImageFile(BlobProxy.generateProxy(photo.getInputStream(), photo.getSize()));
            
            img.setProduct(newProduct); 
            
            newProduct.getImages().add(img); 
        }

        productRepository.save(newProduct);

        return "redirect:/user-products-page/" + seller.getUserId();
    }

    // Method to delete a product from the database
    //Esto va en service, en logica de negocio, esto y todo las funciones
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
    

    /**To be implemented in the future
    @GetMapping("/favorite-products-page/{id}")
    public String showFavorites(@PathVariable long id, Model model) {
        User user = userRepository.findById(id).orElseThrow();
    
        // We pass the list of favorites to the template 
        model.addAttribute("favoriteProducts", user.getFavoriteProducts());
        model.addAttribute("userId", id);
    
        return "favorite-products-page"; // Name of your .html file
    }

    @PostMapping("/add-favorite/{id}")
    public String addFavorite(@PathVariable long id, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        if (principal == null) return "redirect:/login-page";

        User user = userRepository.findByName(principal.getName()).orElseThrow();

        Product product = productRepository.findById(id).orElseThrow();
        
        user.addFavorite(product);
        //update user
        userRepository.save(user);

        return "redirect:/";
    }**/

    @GetMapping("/user-setting-page/{id}")
    public String showUserettings(Model model, @PathVariable long id, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return "redirect:/login-page";
        }

        User loggedInUser = userRepository.findByName(principal.getName()).orElseThrow();
        // 3. SECURITY CHECK: Ensure the user can only access their own settings
        if (loggedInUser.getUserId() != id) {
            return "redirect:/error"; // Or access denied page
        }

        //we add all the user object 
        model.addAttribute("user", loggedInUser);

        return "user-setting-page";

    }

    @PostMapping("/user-settings/edit/{id}")
    public String updateSettings(@PathVariable long id, 
                                @RequestParam(required = false) MultipartFile newProfilePhoto,
                                @RequestParam(required = false) String newEmail,
                                @RequestParam(required = false) String newCardNumber,
                                @RequestParam(required = false) String newCardCvv,
                                @RequestParam(required = false) String newCardExpiringDate, 
                                @RequestParam(required = false) String newDescription,
                                Principal principal) throws IOException {
        
        // 1. Security Check: Verify user identity
        User currentUser = userRepository.findByName(principal.getName()).orElseThrow();
        if (currentUser.getUserId() != id) {
            return "redirect:/error?msg=unauthorized";
        }

        // 2. Delegate to Service 
        userService.updateUserSettings(id, newProfilePhoto, newEmail, newCardNumber, newCardCvv, newCardExpiringDate, newDescription);

        // 3. Redirect back with success flag
        return "redirect:/user-setting-page/" + id + "?updated=true";
    }

    @PostMapping("/user-settings/delete/{id}")
    public String deleteUserInSettings(@PathVariable long id, Principal principal, HttpServletRequest request) throws ServletException {
        
        // 1. Security Check: Verify user identity
        User currentUser = userRepository.findByName(principal.getName()).orElseThrow();
        if (currentUser.getUserId() != id) {
            return "redirect:/error?msg=unauthorized";
        }

        // 2. Delegate to Service 
        userService.deleteUser(id);

        // 3. Manualy logout
        request.logout();

        // 4. Redirect back to index
        return "redirect:/";
    }

}