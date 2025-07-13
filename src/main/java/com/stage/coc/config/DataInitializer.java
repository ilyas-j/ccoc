package com.stage.coc.config;

import com.stage.coc.entity.*;
import com.stage.coc.enums.TypeUser;
import com.stage.coc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BureauControleRepository bureauControleRepository;
    private final ImportateurRepository importateurRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialiser les bureaux de contrôle
        initBureauxControle();

        // Initialiser les utilisateurs de test
        initTestUsers();
    }

    private void initBureauxControle() {
        String[] bureauxNames = {"TUV", "ECF", "AFNOR", "ICUM", "SGS"};

        for (String nom : bureauxNames) {
            if (!bureauControleRepository.findByNom(nom).isPresent()) {
                BureauControle bureau = new BureauControle();
                bureau.setNom(nom);
                bureau.setAdresse("Adresse " + nom + ", Casablanca");
                bureau.setTelephone("+212522123456");
                bureau.setEmail("contact@" + nom.toLowerCase() + ".ma");
                bureauControleRepository.save(bureau);
            }
        }
    }

    private void initTestUsers() {
        // Créer un importateur de test
        if (!userRepository.findByEmail("importateur@test.ma").isPresent()) {
            User userImportateur = new User();
            userImportateur.setEmail("importateur@test.ma");
            userImportateur.setPassword(passwordEncoder.encode("password"));
            userImportateur.setNom("Société Import Maroc");
            userImportateur.setTelephone("+212661234567");
            userImportateur.setTypeUser(TypeUser.IMPORTATEUR);
            userImportateur = userRepository.save(userImportateur);

            Importateur importateur = new Importateur();
            importateur.setUser(userImportateur);
            importateur.setRaisonSociale("Société Import Maroc SARL");
            importateur.setAdresse("123 Bd Mohammed V, Casablanca");
            importateur.setCodeDouane("IMP001");
            importateur.setIce("001234567890123");
            importateurRepository.save(importateur);
        }

        // Créer un exportateur de test
        if (!userRepository.findByEmail("exportateur@test.com").isPresent()) {
            User userExportateur = new User();
            userExportateur.setEmail("exportateur@test.com");
            userExportateur.setPassword(passwordEncoder.encode("password"));
            userExportateur.setNom("Société Export France");
            userExportateur.setTelephone("+33123456789");
            userExportateur.setTypeUser(TypeUser.EXPORTATEUR);
            userRepository.save(userExportateur);
        }

        // Créer des agents de test
        BureauControle tuv = bureauControleRepository.findByNom("TUV").orElse(null);
        if (tuv != null) {
            // Agent simple
            if (!userRepository.findByEmail("agent1@tuv.ma").isPresent()) {
                User userAgent = new User();
                userAgent.setEmail("agent1@tuv.ma");
                userAgent.setPassword(passwordEncoder.encode("password"));
                userAgent.setNom("Agent Dupont");
                userAgent.setTelephone("+212661111111");
                userAgent.setTypeUser(TypeUser.AGENT);
                userAgent = userRepository.save(userAgent);

                Agent agent = new Agent();
                agent.setUser(userAgent);
                agent.setBureauControle(tuv);
                agent.setDisponible(true);
                agent.setEnConge(false);
                agent.setChargeTravail(0);
                agent.setSuperviseur(false);
                agentRepository.save(agent);
            }

            // Superviseur
            if (!userRepository.findByEmail("superviseur@tuv.ma").isPresent()) {
                User userSuperviseur = new User();
                userSuperviseur.setEmail("superviseur@tuv.ma");
                userSuperviseur.setPassword(passwordEncoder.encode("password"));
                userSuperviseur.setNom("Superviseur Martin");
                userSuperviseur.setTelephone("+212662222222");
                userSuperviseur.setTypeUser(TypeUser.AGENT); // Le superviseur est un agent avec privilèges
                userSuperviseur = userRepository.save(userSuperviseur);

                Agent superviseur = new Agent();
                superviseur.setUser(userSuperviseur);
                superviseur.setBureauControle(tuv);
                superviseur.setDisponible(true);
                superviseur.setEnConge(false);
                superviseur.setChargeTravail(0);
                superviseur.setSuperviseur(true);
                agentRepository.save(superviseur);
            }
        }
    }
}