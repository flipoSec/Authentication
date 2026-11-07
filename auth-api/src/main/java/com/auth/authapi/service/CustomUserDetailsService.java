package com.auth.authapi.service;


import com.auth.authapi.entity.User;
import com.auth.authapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public CustomUserDetails loadUserByUsername(String username){
        Optional<User> user = userRepo.findByEmail(username);
        if(user.isPresent()){
            return new CustomUserDetails(user.get());
        }
        throw new UsernameNotFoundException("User Not Found");
    }



}
