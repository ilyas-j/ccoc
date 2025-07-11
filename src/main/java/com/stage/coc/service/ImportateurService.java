package com.stage.coc.service;

import com.stage.coc.entity.Importateur;
import com.stage.coc.entity.User;
import com.stage.coc.enums.TypeUser;
import com.stage.coc.repository.ImportateurRepository;
import com.stage.coc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ImportateurService {

    private final UserRepository userRepository;
    private final ImportateurRepository importateurRepository;
    private final PasswordEncoder passwordEncoder;

    public User creerImportateur(String email, String password, String nom, String telephone,
                                 String raisonSociale, String adresse, String codeDouane, String ice) {

        // Créer l'utilisateur
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNom(nom);
        user.setTelephone(telephone);
        user.setTypeUser(TypeUser.IMPORTATEUR);
        user = userRepository.save(user);

        // Créer l'importateur
        Importateur importateur = new Importateur();
        importateur.setUser(user);
        importateur.setRaisonSociale(raisonSociale);
        importateur.setAdresse(adresse);
        importateur.setCodeDouane(codeDouane);
        importateur.setIce(ice);
        importateurRepository.save(importateur);

        return user;
    }
}