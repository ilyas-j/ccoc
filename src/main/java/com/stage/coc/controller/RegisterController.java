package com.stage.coc.controller;

import com.stage.coc.dto.request.RegisterRequest;
import com.stage.coc.dto.response.MessageResponse;
import com.stage.coc.entity.BureauControle;
import com.stage.coc.entity.User;
import com.stage.coc.repository.BureauControleRepository;
import com.stage.coc.repository.UserRepository;
import com.stage.coc.service.RegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RegisterController {

    private final RegistrationService registrationService;
    private final BureauControleRepository bureauControleRepository;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = registrationService.registerUser(registerRequest);
            return ResponseEntity.ok(new MessageResponse("Inscription réussie ! Vous pouvez maintenant vous connecter."));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/bureaux-controle")
    public ResponseEntity<List<BureauControle>> getBureauxControle() {
        List<BureauControle> bureaux = bureauControleRepository.findAll();
        return ResponseEntity.ok(bureaux);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailable(@RequestParam String email) {
        boolean available = !userRepository.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        return ResponseEntity.ok(response);
    }
}