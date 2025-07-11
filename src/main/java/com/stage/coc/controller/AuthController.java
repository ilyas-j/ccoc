package com.stage.coc.controller;

import com.stage.coc.dto.request.LoginRequest;
import com.stage.coc.dto.request.RegisterRequest;
import com.stage.coc.dto.response.AuthResponse;
import com.stage.coc.entity.User;
import com.stage.coc.service.AuthService;
import com.stage.coc.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final RegisterService registerService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = registerService.register(registerRequest);
        return ResponseEntity.ok("Utilisateur créé avec succès. Vous pouvez maintenant vous connecter.");
    }

    @GetMapping("/bureaux-controle")
    public ResponseEntity<?> getBureauxControle() {
        // Endpoint pour récupérer la liste des bureaux de contrôle
        return ResponseEntity.ok("Liste des bureaux"); // À implémenter
    }
}
