package com.harbaoui.iot.user_service.service;

import com.harbaoui.iot.user_service.dto.LoginRequest;
import com.harbaoui.iot.user_service.entity.User;
import com.harbaoui.iot.user_service.entity.VerificationToken;
import com.harbaoui.iot.user_service.exception.AccountNotVerifiedException;
import com.harbaoui.iot.user_service.exception.InvalidCredentialsException;
import com.harbaoui.iot.user_service.exception.UserAlreadyExistsException;
import com.harbaoui.iot.user_service.repository.UserRepository;
import com.harbaoui.iot.user_service.repository.VerificationTokenRepository;
import com.harbaoui.iot.user_service.exception.UserNotFoundException;
import com.harbaoui.iot.user_service.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public UserService(
        UserRepository userRepository,
        VerificationTokenRepository verificationTokenRepository,
        MailService mailService,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
 
    }

    // Create or update a user
    // This method is used to register a new user or update an existing user.
    // It accepts a User object and returns the saved user.
    public User saveUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));  // Encode password
        user.setVerified(false);
        User savedUser = userRepository.save(user);

        generateVerificationToken(savedUser);  // Generate verification token for the user

        return savedUser;
    }

    // Find all users
    // This method is used to retrieve all users from the database.
    // It returns a list of User objects.
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Find a user by ID
    // This method is used to retrieve a user by their ID.
    // It returns an Optional<User> object.
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    // Find a user by email
    // This method is used to retrieve a user by their email address.
    // It returns an Optional<User> object.
    public Optional<User> findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        return user;
    }

    // Delete a user by ID
    // This method is used to delete a user by their ID.
    // It accepts a user ID and deletes the user from the database.
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Delete all users
    // This method is used to delete all users from the database.
    // It deletes all user records from the database.
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    // Generate Verification Token
    // This method is used to generate a verification token for the user.
    // It creates a new VerificationToken object and saves it to the database.
    private void generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();
        verificationTokenRepository.save(verificationToken);
    
        String verificationLink = "http://localhost:8080/users/verify?token=" + token;
    
        try {
            mailService.sendVerificationEmail(user.getEmail(), verificationLink);
        } catch (MessagingException e) {
            throw new InvalidCredentialsException("Unable to send verification link. Please check your email address.");
        }
    }
    
    
    public void resendVerificationLink(String email) {
    // Find user by email
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

    // Check if the account is already verified
    if (user.isVerified()) {
        throw new AccountNotVerifiedException("Account is already verified.");
    }

    // Check if a verification token already exists
    Optional<VerificationToken> existingToken = verificationTokenRepository.findByUser(user);

    // If an existing token is found, check if it's expired
    if (existingToken.isPresent()) {
        VerificationToken token = existingToken.get();
        
        // If the token is not expired, we can reuse it
        if (token.getExpiryDate().isAfter(LocalDateTime.now())) {
            // Don't create a new token, just resend the existing one
            String verificationLink = "http://localhost:8080/users/verify?token=" + token.getToken();
            try {
                mailService.sendVerificationEmail(user.getEmail(), verificationLink);
            } catch (MessagingException e) {
                throw new InvalidCredentialsException("Unable to send verification link. Please check your email address.");
            }
            return;
        }

        // If the token is expired, delete it and create a new one
        verificationTokenRepository.delete(token);
    }

    // Generate a new verification token if none exists or the previous one was expired
    generateVerificationToken(user);
}



    // Verify user  
    // This method is used to verify a user's email address using a verification token.
    public String verifyUser(String token) {
        Optional<VerificationToken> optionalToken = verificationTokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            return "Invalid verification token.";
        }

        VerificationToken verificationToken = optionalToken.get();

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "Token has expired.";
        }

        User user = verificationToken.getUser();
        user.setVerified(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        return "Email successfully verified!";
    }

    // Login method
    // This method is simplified to directly validate the password without using AuthenticationManager.
    // Inside the UserService login method
    public String login(LoginRequest loginRequest) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    
        // Check if the account is verified
        if (!user.isVerified()) {
            throw new AccountNotVerifiedException("Account not activated yet. Please check your inbox for the verification link.");
        }
    
        // Validate password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    
        // Generate JWT token using JwtService
        return jwtService.generateJwtToken(user.getEmail());
    }
    
    
    
    
}
