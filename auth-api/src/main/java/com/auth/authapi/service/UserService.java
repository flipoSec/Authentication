package com.auth.authapi.service;

import com.auth.authapi.entity.User;
import com.auth.authapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> getUserById(Long id) {
        return userRepo.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public Optional<User> updateUser(Long id, User user) {

        Optional<User> oldUser = userRepo.findById(id);

        if (oldUser.isEmpty()) {
            return Optional.empty();
        }

        User existingUser = oldUser.get();

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        existingUser.setPassword(hashedPassword);

        return Optional.of(userRepo.save(existingUser));
    }

    public boolean deleteUserById(Long id) {

        if (userRepo.existsById(id)) {
            userRepo.deleteById(id);
            return true;
        }

        return false;
    }
}
