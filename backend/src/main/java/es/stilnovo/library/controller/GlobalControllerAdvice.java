package es.stilnovo.library.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import es.stilnovo.library.model.User;
import es.stilnovo.library.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        
        if (principal != null) {
            // Search for the user by their email/username (the one you use to log in)
            User user = userRepository.findByName(principal.getName()).orElse(null);
            
            if (user != null) {
                model.addAttribute("logged", true);
                model.addAttribute("username", user.getName());
                model.addAttribute("userId", user.getUserId());
            }
        } else {
            model.addAttribute("logged", false);
        }

        // Always send the CSRF token to avoid 403 error on logout
        Object csrf = request.getAttribute("_csrf");
        if (csrf != null) {
            model.addAttribute("token", ((org.springframework.security.web.csrf.CsrfToken) csrf).getToken());
        }
    }
}