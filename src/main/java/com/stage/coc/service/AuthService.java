package com.stage.coc.service;

import com.stage.coc.dto.request.LoginRequest;
import com.stage.coc.dto.response.AuthResponse;
import com.stage.coc.entity.User;
import com.stage.coc.exception.UnauthorizedException;
import com.stage.coc.repository.UserRepository;
import com.stage.coc.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non trouvé"));

        return new AuthResponse(jwt, "Bearer", user.getId(), user.getEmail(),
                user.getNom(), user.getTypeUser());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Si c'est un ID numérique, chercher par ID
        if (username.matches("\\d+")) {
            User user = userRepository.findById(Long.parseLong(username))
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'ID: " + username));
            return user;
        }

        // Sinon chercher par email
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + username));
        return user;
    }
}