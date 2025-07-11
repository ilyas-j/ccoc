package com.stage.coc.service;

import com.stage.coc.dto.request.RegisterRequest;
import com.stage.coc.entity.*;
import com.stage.coc.enums.TypeUser;
import com.stage.coc.exception.ValidationException;
import com.stage.coc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterService {

    private final UserRepository userRepository;
    private final ImportateurRepository importateurRepository;
    private final AgentRepository agentRepository;
    private final BureauControleRepository bureauControleRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Cet email est déjà utilisé");
        }

        // Créer l'utilisateur de base
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNom(request.getNom());
        user.setTelephone(request.getTelephone());
        user.setTypeUser(request.getTypeUser());
        user = userRepository.save(user);

        // Créer le profil spécifique selon le type
        switch (request.getTypeUser()) {
            case IMPORTATEUR:
                createImportateurProfile(user, request);
                break;
            case AGENT:
                createAgentProfile(user, request);
                break;
            default:
                throw new ValidationException("Type d'utilisateur non supporté pour l'inscription");
        }

        return user;
    }

    private void createImportateurProfile(User user, RegisterRequest request) {
        Importateur importateur = new Importateur();
        importateur.setUser(user);
        importateur.setRaisonSociale(request.getRaisonSociale());
        importateur.setAdresse(request.getAdresse());
        importateur.setCodeDouane(request.getCodeDouane());
        importateur.setIce(request.getIce());
        importateurRepository.save(importateur);
    }

    private void createAgentProfile(User user, RegisterRequest request) {
        BureauControle bureau = bureauControleRepository.findById(request.getBureauControleId())
                .orElseThrow(() -> new ValidationException("Bureau de contrôle non trouvé"));

        Agent agent = new Agent();
        agent.setUser(user);
        agent.setBureauControle(bureau);
        agent.setSuperviseur(request.isSuperviseur());
        agent.setDisponible(true);
        agent.setEnConge(false);
        agent.setChargeTravail(0);
        agentRepository.save(agent);
    }
}