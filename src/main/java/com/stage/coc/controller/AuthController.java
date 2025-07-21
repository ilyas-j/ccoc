package com.stage.coc.controller;

import com.stage.coc.dto.request.LoginRequest;
import com.stage.coc.dto.request.RegisterRequest;
import com.stage.coc.dto.response.AuthResponse;
import com.stage.coc.dto.response.MessageResponse;
import com.stage.coc.entity.BureauControle;
import com.stage.coc.entity.User;
import com.stage.coc.repository.BureauControleRepository;
import com.stage.coc.service.AuthService;
import com.stage.coc.service.RegistrationService;
import com.stage.coc.dto.response.AuthResponse;
import com.stage.coc.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;
    private final BureauControleRepository bureauControleRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = registrationService.registerUser(registerRequest);
        return ResponseEntity.ok(new MessageResponse("Utilisateur créé avec succès!"));
    }

    @GetMapping("/bureaux-controle")
    public ResponseEntity<List<BureauControle>> getBureauxControle() {
        List<BureauControle> bureaux = bureauControleRepository.findAll();
        return ResponseEntity.ok(bureaux);
    }



}