package es.stilnovo.library.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.stilnovo.library.model.User;
import es.stilnovo.library.repository.UserRepository;
import java.io.IOException;
import java.sql.Blob;

import org.hibernate.engine.jdbc.proxy.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.stilnovo.library.model.User;
import es.stilnovo.library.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }


    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }
    
    public boolean existsUser(String name) {
        return userRepository.findByName(name).isPresent();
    }

    public boolean existsEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
    @Transactional
    public void updateUserSettings(Long userId, MultipartFile newProfilePhoto, String email, String cardNumber, String cardCvv, String cardExpiringDate, String description) throws IOException {
        // 1. Fetch the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (newProfilePhoto != null && !newProfilePhoto.isEmpty()) {
            Blob imageBlob = BlobProxy.generateProxy(
                newProfilePhoto.getInputStream(), 
                newProfilePhoto.getSize()
            );
            user.setProfileImage(imageBlob); // Asegúrate de que el setter se llame así en User.java
        }

        // 2. Partial update for allowed fields only
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email);
        }
        if (cardNumber != null && !cardNumber.trim().isEmpty()) {
            user.setCardNumber(cardNumber);
        }
        if (cardCvv != null && !cardCvv.trim().isEmpty()) {
            user.setCardCvv(cardCvv);
        }
        if (cardExpiringDate != null && !cardExpiringDate.trim().isEmpty()) {
            user.setCardExpiringDate(cardExpiringDate); 
        }
        if (description != null && !description.trim().isEmpty()) {
            user.setUserDescription(description); 
        }

        // 3. Persist changes
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId){
        
        userRepository.deleteById(userId);
    }
    