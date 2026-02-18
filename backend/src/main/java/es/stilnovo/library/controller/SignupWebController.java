package es.stilnovo.library.controller;

import java.io.IOException;
import java.sql.Blob;
import es.stilnovo.library.model.User;

import org.hibernate.engine.jdbc.proxy.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.stilnovo.library.repository.UserRepository;

import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SignupWebController {

    @GetMapping("/error")
    public String signupError() {
        return "error"; 
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/signup-page")
    public String signup(Model model, HttpServletRequest request) {
        // Obtenemos el token para que el formulario sea seguro
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        if (token != null) {
            model.addAttribute("token", token.getToken());
        }
        
        return "signup-page"; 
    }

    @PostMapping("/signup-page")
    public String createAccount(Model model, 
                                @RequestParam MultipartFile profilePicture, 
                                @RequestParam String username,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String confirmPassword) throws IOException{

        // 1. Validamos que las contraseñas sean idénticas
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            // Opcional: pasar el nombre de usuario de vuelta para que no tenga que escribirlo otra vez
            model.addAttribute("username", username);
            return "signup-page"; // Volvemos al formulario con el error
        }

        String encodedPassword = passwordEncoder.encode(password);

        // 2. We process the image if it was uploaded
        Blob imageBlob = null;
        if (profilePicture != null && !profilePicture.isEmpty()) {
            imageBlob = BlobProxy.generateProxy(
                profilePicture.getInputStream(), 
                profilePicture.getSize()
            );
        }else{ //asign a default image

            Resource defaultUserImage = new ClassPathResource("static/images/no-profile-picture.png");
            Blob photoUserBlob = BlobProxy.generateProxy(defaultUserImage.getInputStream(), defaultUserImage.contentLength());
            imageBlob = photoUserBlob;
        }
        
        // 3. We create the user with default values (rating 5.0, 0 reviews)
        // We add ROLE_USER so they can log in later
        User newUser = new User(username, encodedPassword, email, imageBlob, 5.0, null, null, null , 0, 0.0, 0.0, null, "ROLE_USER");

        // 4. Save to Docker database
        userRepository.save(newUser);

        //If signup ok, redirect
        return "redirect:/login-page";
    }
}