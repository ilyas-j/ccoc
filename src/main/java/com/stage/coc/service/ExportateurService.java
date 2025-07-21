package com.stage.coc.service;

import com.stage.coc.entity.Exportateur;
import com.stage.coc.entity.User;
import com.stage.coc.enums.TypeUser;
import com.stage.coc.repository.ExportateurRepository;
import com.stage.coc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExportateurService {

    private final UserRepository userRepository;
    private final ExportateurRepository exportateurRepository;
    private final PasswordEncoder passwordEncoder;

    public User creerExportateur(String email, String password, String nom, String telephone,
                                 String raisonSociale, String adresse, String pays, String ifu) {

        // Créer l'utilisateur
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNom(nom);
        user.setTelephone(telephone);
        user.setTypeUser(TypeUser.EXPORTATEUR); // ✅ UTILISER LE NOUVEAU TYPE
        user = userRepository.save(user);

        // Créer l'exportateur
        Exportateur exportateur = new Exportateur();
        exportateur.setUser(user);
        exportateur.setRaisonSociale(raisonSociale);
        exportateur.setAdresse(adresse);
        exportateur.setPays(pays);
        exportateur.setIfu(ifu);
        exportateurRepository.save(exportateur);

        return user;
    }

    public Exportateur getOrCreateExportateur(String email, String raisonSociale, String pays) {
        // Chercher d'abord par user email s'il existe
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.getTypeUser() == TypeUser.EXPORTATEUR) {
            return exportateurRepository.findByUserId(user.getId()).orElse(null);
        }

        // Sinon chercher par raison sociale et pays
        return exportateurRepository.findByRaisonSocialeAndPays(raisonSociale, pays).orElse(null);
    }
}