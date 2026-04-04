package com.example.demo.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.AppUser;
import com.example.demo.repository.AppUserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/signup")
    public String showSignupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@RequestParam String username, @RequestParam String password, Model model, HttpServletRequest request) {
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists!");
            return "signup";
        }
        
        AppUser newUser = AppUser.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role("ADMIN")
                .build();
        
        userRepository.save(newUser);
        
        try {
            request.login(username, password);
        } catch (ServletException e) {
            return "redirect:/login?success";
        }

        return "redirect:/";
    }
}