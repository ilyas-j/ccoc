package com.stage.coc.service;

import com.stage.coc.dto.request.LoginRequest;
import com.stage.coc.dto.response.AuthResponse;
import com.stage.coc.entity.User;
import com.stage.coc.exception.UnauthorizedException;
import com.stage.coc.repository.UserRepository;
import com.stage.coc.security.JwtTokenProvider;
import com.stage.coc.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        System.out.println("🔐 AuthService: Tentative d'authentification pour: " + loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            System.out.println("✅ AuthService: Authentification réussie pour: " + loginRequest.getEmail());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            // ✅ Récupérer les informations utilisateur pour la réponse
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("Utilisateur non trouvé"));

            System.out.println("✅ AuthService: Token JWT généré pour: " + user.getEmail());

            return new AuthResponse(jwt, "Bearer", user.getId(), user.getEmail(),
                    user.getNom(), user.getTypeUser());

        } catch (Exception e) {
            System.err.println("❌ AuthService: Erreur d'authentification pour " + loginRequest.getEmail() + ": " + e.getMessage());
            throw e;
        }
    }
}