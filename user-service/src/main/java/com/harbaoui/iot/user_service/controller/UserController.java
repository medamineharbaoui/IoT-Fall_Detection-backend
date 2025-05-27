package com.harbaoui.iot.user_service.controller;

import com.harbaoui.iot.user_service.entity.User;
import com.harbaoui.iot.user_service.exception.AccountNotVerifiedException;
import com.harbaoui.iot.user_service.exception.InvalidCredentialsException;
import com.harbaoui.iot.user_service.exception.UserAlreadyExistsException;
import com.harbaoui.iot.user_service.service.UserService;
import com.harbaoui.iot.user_service.dto.LoginRequest;
import com.harbaoui.iot.user_service.dto.LoginResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    // Create a user
    // This endpoint is used to register a new user.
    // It accepts a User object in the request body and returns the created user with a 201 Created status.
    // If the email already exists, it returns a 400 Bad Request status.
    @PostMapping ("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User savedUser = userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
        }
    }
    

    // Find all users
    // This endpoint is used to retrieve all users from the database.
    // It returns a 200 OK response with a list of users.
    @GetMapping ("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get a user by ID
    // This endpoint is used to retrieve a user by their ID.
    // It returns a 200 OK response with the user details if found, or a 404 Not Found response if not found.
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get a user by email
    // This endpoint is used to retrieve a user by their email address.
    // It returns a 200 OK response with the user details if found, or a 404 Not Found response if not found.
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.findUserByEmail(email);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a user
    // This endpoint is used to delete a user by their ID.
    // It returns a 204 No Content response if the user is successfully deleted.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    // Delete all users
    // This endpoint is used to delete all users from the database.
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllUsers() {
        userService.deleteAllUsers();
        return ResponseEntity.noContent().build();
    }

    // Verify user
    // This endpoint is used to verify a user's email address using a verification token.
    // The token is sent as a request parameter.
    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String token) {
        String result = userService.verifyUser(token);
    
        if (result.equals("Email successfully verified!")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    // Login
    // This endpoint is used to authenticate a user and generate a JWT token.
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Delegate the login process to UserService
            String token = userService.login(request);
    
            // Return the generated token in the response
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (AccountNotVerifiedException e) {
            // Handle unverified account scenario
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (RuntimeException e) {
            // Handle user not found or invalid credentials scenario
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
        }
    }
    
    // Resend verification link
    // This endpoint is used to resend the email verification link to the user based on their email address.
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationLink(@RequestParam String email) {
    try {
        // Delegate the process to the UserService for resending the verification email.
        userService.resendVerificationLink(email);
        return ResponseEntity.ok("Verification link has been resent.");
    } catch (Exception e) {
        // Handle any exceptions such as user not found, email not verified, etc.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error resending verification link: " + e.getMessage());
    }
}

}
