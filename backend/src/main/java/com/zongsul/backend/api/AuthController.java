package com.zongsul.backend.api;

import com.zongsul.backend.domain.user.User;
import com.zongsul.backend.domain.user.UserRepository;
import com.zongsul.backend.domain.user.UserRole;
import com.zongsul.backend.security.JwtTokenProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

public record RegisterRequest(@Email String email, @NotBlank String name, @NotBlank String password, String role) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) return ResponseEntity.badRequest().body(Map.of("error","email exists"));
        UserRole role = (req.role() == null) ? UserRole.USER : UserRole.valueOf(req.role().toUpperCase());
        User u = new User(req.email(), req.name(), passwordEncoder.encode(req.password()), role);
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("id", u.getId(), "email", u.getEmail(), "name", u.getName(), "role", u.getRole()));
    }

    public record LoginRequest(@Email String email, @NotBlank String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User u = userRepository.findByEmail(req.email()).orElse(null);
        if (u == null || !passwordEncoder.matches(req.password(), u.getPassword()))
            return ResponseEntity.status(401).body(Map.of("error","invalid credentials"));
        String token = jwtTokenProvider.createToken(u.getEmail(), u.getRole().name(), u.getName());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User u = userRepository.findByEmail(principal.getName()).orElse(null);
        if (u == null) return ResponseEntity.status(404).build();
        return ResponseEntity.ok(Map.of("id", u.getId(), "email", u.getEmail(), "name", u.getName(), "role", u.getRole()));
    }
}
