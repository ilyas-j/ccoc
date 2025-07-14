package com.stage.coc.config;

import com.stage.coc.entity.*;
import com.stage.coc.enums.TypeUser;
import com.stage.coc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ImportateurRepository importateurRepository;
    private final BureauControleRepository bureauControleRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initBureauxControle();
        initUtilisateursDemo();
    }

    private void initBureauxControle() {
        String[] bureauxNoms = {"TUV", "ECF", "AFNOR", "ICUM", "SGS"};

        for (String nom : bureauxNoms) {
            if (bureauControleRepository.findByNom(nom).isEmpty()) {
                BureauControle bureau = new BureauControle();
                bureau.setNom(nom);
                bureau.setAdresse("Adresse " + nom);
                bureau.setTelephone("+212 5XX XX XX XX");
                bureau.setEmail("contact@" + nom.toLowerCase() + ".ma");
                bureauControleRepository.save(bureau);
            }
        }
    }

    private void initUtilisateursDemo() {
        // Importateur Demo
        if (!userRepository.existsByEmail("importateur@test.ma")) {
            User userImportateur = new User();
            userImportateur.setEmail("importateur@test.ma");
            userImportateur.setPassword(passwordEncoder.encode("password"));
            userImportateur.setNom("Société Import Maroc");
            userImportateur.setTelephone("+212666123456");
            userImportateur.setTypeUser(TypeUser.IMPORTATEUR);
            userImportateur = userRepository.save(userImportateur);

            Importateur importateur = new Importateur();
            importateur.setUser(userImportateur);
            importateur.setRaisonSociale("Société Import Maroc");
            importateur.setAdresse("123 Rue du Commerce, Casablanca");
            importateur.setCodeDouane("CD123456");
            importateur.setIce("ICE123456789");
            importateurRepository.save(importateur);
        }

        // Agent Demo
        if (!userRepository.existsByEmail("agent1@tuv.ma")) {
            User userAgent = new User();
            userAgent.setEmail("agent1@tuv.ma");
            userAgent.setPassword(passwordEncoder.encode("password"));
            userAgent.setNom("Agent TUV");
            userAgent.setTelephone("+212666234567");
            userAgent.setTypeUser(TypeUser.AGENT);
            userAgent = userRepository.save(userAgent);

            BureauControle tuv = bureauControleRepository.findByNom("TUV").orElse(null);
            if (tuv != null) {
                Agent agent = new Agent();
                agent.setUser(userAgent);
                agent.setBureauControle(tuv);
                agent.setDisponible(true);
                agent.setEnConge(false);
                agent.setChargeTravail(0);
                agent.setSuperviseur(false);
                agentRepository.save(agent);
            }
        }

        // Superviseur Demo
        if (!userRepository.existsByEmail("superviseur@tuv.ma")) {
            User userSuperviseur = new User();
            userSuperviseur.setEmail("superviseur@tuv.ma");
            userSuperviseur.setPassword(passwordEncoder.encode("password"));
            userSuperviseur.setNom("Superviseur TUV");
            userSuperviseur.setTelephone("+212666345678");
            userSuperviseur.setTypeUser(TypeUser.SUPERVISEUR);
            userSuperviseur = userRepository.save(userSuperviseur);

            BureauControle tuv = bureauControleRepository.findByNom("TUV").orElse(null);
            if (tuv != null) {
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