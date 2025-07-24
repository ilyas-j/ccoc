package com.stage.coc.config;

import com.stage.coc.entity.*;
import com.stage.coc.enums.TypeUser;
import com.stage.coc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@DependsOn({"userRepository", "importateurRepository", "exportateurRepository"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ImportateurRepository importateurRepository;
    private final ExportateurRepository exportateurRepository;
    private final BureauControleRepository bureauControleRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initBureauxControle();
        initUtilisateursDemo();
        // ✅ SUPPRIMER COMPLÈTEMENT : initDemandesDemo();
        System.out.println("✅ Initialisation terminée - SANS demandes de démonstration");
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
                System.out.println("✅ Bureau de contrôle créé: " + nom);
            }
        }
    }

    private void initUtilisateursDemo() {
        // ✅ GARDER SEULEMENT LES UTILISATEURS DE TEST (sans leurs demandes)

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
            System.out.println("✅ Importateur de test créé: importateur@test.ma / password");
        }

        // Exportateur Demo
        if (!userRepository.existsByEmail("exportateur@test.com")) {
            User userExportateur1 = new User();
            userExportateur1.setEmail("exportateur@test.com");
            userExportateur1.setPassword(passwordEncoder.encode("password"));
            userExportateur1.setNom("Exportateur France");
            userExportateur1.setTelephone("+33123456789");
            userExportateur1.setTypeUser(TypeUser.EXPORTATEUR);
            userExportateur1 = userRepository.save(userExportateur1);

            Exportateur exportateur1 = new Exportateur();
            exportateur1.setUser(userExportateur1);
            exportateur1.setRaisonSociale("Société Export France");
            exportateur1.setTelephone("+33123456789");
            exportateur1.setEmail("exportateur@test.com");
            exportateur1.setAdresse("456 Avenue Export, Lyon");
            exportateur1.setPays("France");
            exportateur1.setIfu("FR123456789");
            exportateurRepository.save(exportateur1);
            System.out.println("✅ Exportateur de test créé: exportateur@test.com / password");
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
                System.out.println("✅ Agent de test créé: agent1@tuv.ma / password");
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
                System.out.println("✅ Superviseur de test créé: superviseur@tuv.ma / password");
            }
        }
    }

    // ✅ MÉTHODE SUPPRIMÉE COMPLÈTEMENT
    // private void initDemandesDemo() { ... }
}