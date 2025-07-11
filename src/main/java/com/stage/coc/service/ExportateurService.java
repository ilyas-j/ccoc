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
                                 String raisonSociale, String pays, String adresse, String ville,
                                 String codePostal, String ifu, String numeroExportateur,
                                 String secteurActivite, String numeroRegistre) {

        // Créer l'utilisateur
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNom(nom);
        user.setTelephone(telephone);
        user.setTypeUser(TypeUser.EXPORTATEUR);
        user = userRepository.save(user);

        // Créer le profil exportateur
        Exportateur exportateur = new Exportateur();
        exportateur.setUser(user);
        exportateur.setRaisonSociale(raisonSociale);
        exportateur.setPays(pays);
        exportateur.setAdresse(adresse);
        exportateur.setVille(ville);
        exportateur.setCodePostal(codePostal);
        exportateur.setIfu(ifu);
        exportateur.setNumeroExportateur(numeroExportateur);
        exportateur.setSecteurActivite(secteurActivite);
        exportateur.setNumeroRegistre(numeroRegistre);
        exportateurRepository.save(exportateur);

        return user;
    }

    public Exportateur getExportateur(Long userId) {
        return exportateurRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil exportateur non trouvé"));
    }
}