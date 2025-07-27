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
public class RegistrationService {

    private final UserRepository userRepository;
    private final ImportateurRepository importateurRepository;
    private final AgentRepository agentRepository;
    private final BureauControleRepository bureauControleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExportateurRepository exportateurRepository;
    private final SuperviseurRepository superviseurRepository;


    public User registerUser(RegisterRequest request) {
        // ✅ VALIDATIONS RENFORCÉES
        validateRegistrationRequest(request);

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Cette adresse email est déjà utilisée");
        }

        // Créer l'utilisateur
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNom(request.getNom());
        user.setTelephone(request.getTelephone());
        user.setTypeUser(request.getTypeUser());
        user = userRepository.save(user);

        // Créer le profil spécifique
        switch (request.getTypeUser()) {
            case IMPORTATEUR:
                createImportateur(user, request);
                break;
            case EXPORTATEUR:
                createExportateur(user, request);
                break;
            case AGENT:
                createAgent(user, request);
                break;
            case SUPERVISEUR:
                createSuperviseur(user, request);
                break;
        }

        return user;
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        // Validation mot de passe fort
        if (request.getPassword().length() < 8) {
            throw new ValidationException("Le mot de passe doit contenir au moins 8 caractères");
        }

        // Validation selon le type d'utilisateur
        switch (request.getTypeUser()) {
            case IMPORTATEUR:
                if (request.getRaisonSociale() == null || request.getRaisonSociale().trim().isEmpty()) {
                    throw new ValidationException("La raison sociale est obligatoire pour un importateur");
                }
                break;
            case EXPORTATEUR:
                if (request.getRaisonSociale() == null || request.getPays() == null) {
                    throw new ValidationException("La raison sociale et le pays sont obligatoires pour un exportateur");
                }
                break;
            case AGENT:
            case SUPERVISEUR:
                if (request.getBureauControleId() == null) {
                    throw new ValidationException("Le bureau de contrôle est obligatoire pour un agent/superviseur");
                }
                break;
        }
    }

    private void createSuperviseur(User user, RegisterRequest request) {
        BureauControle bureau = bureauControleRepository.findById(request.getBureauControleId())
                .orElseThrow(() -> new ValidationException("Bureau de contrôle non trouvé"));

        Superviseur superviseur = new Superviseur();
        superviseur.setUser(user);
        superviseur.setBureauControle(bureau);
        superviseur.setPeutReaffecter(true);
        superviseur.setPeutGererAgents(true);
        superviseur.setPeutVoirToutesLesDemandes(true);
        superviseurRepository.save(superviseur);
    }

    private void createExportateur(User user, RegisterRequest request) {
        Exportateur exportateur = new Exportateur();
        exportateur.setUser(user);
        exportateur.setRaisonSociale(request.getRaisonSociale());
        exportateur.setTelephone(request.getTelephone());
        exportateur.setEmail(request.getEmail());
        exportateur.setAdresse(request.getAdresse());
        exportateur.setPays(request.getPays());
        exportateur.setIfu(request.getIfu());
        exportateurRepository.save(exportateur);
    }

    private void createImportateur(User user, RegisterRequest request) {
        Importateur importateur = new Importateur();
        importateur.setUser(user);
        importateur.setRaisonSociale(request.getRaisonSociale());
        importateur.setAdresse(request.getAdresse());
        importateur.setCodeDouane(request.getCodeDouane());
        importateur.setIce(request.getIce());
        importateurRepository.save(importateur);
    }

    private void createAgent(User user, RegisterRequest request) {
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