package es.stilnovo.library.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.stilnovo.library.model.User;
import es.stilnovo.library.repository.UserRepository;

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