package com.stage.coc.service;

import com.stage.coc.entity.User;
import com.stage.coc.repository.UserRepository;
import com.stage.coc.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("userDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 CustomUserDetailsService: Tentative de connexion pour: " + username);

        User user;

        // Si c'est un ID numérique, chercher par ID
        if (username.matches("\\d+")) {
            user = userRepository.findById(Long.parseLong(username))
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'ID: " + username));
            System.out.println("✅ Utilisateur trouvé par ID: " + user.getEmail());
        } else {
            // Sinon chercher par email
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + username));
            System.out.println("✅ Utilisateur trouvé par email: " + user.getEmail() + " - Type: " + user.getTypeUser());
        }

        // ✅ IMPORTANT: Retourner UserPrincipal au lieu de User directement
        return UserPrincipal.create(user);
    }
}