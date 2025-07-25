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
@DependsOn({"userRepository", "importateurRepository", "exportateurRepository", "superviseurRepository"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ImportateurRepository importateurRepository;
    private final ExportateurRepository exportateurRepository;
    private final BureauControleRepository bureauControleRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;
    private final SuperviseurRepository superviseurRepository;

    @Override
    public void run(String... args) throws Exception {
        initBureauxControle();
        initUtilisateursDemo();
        System.out.println("✅ Initialisation terminée - Application prête pour production");
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
        // Importateur Demo (pour les tests uniquement)
        if (!userRepository.existsByEmail("importateur@test.ma")) {
            User userImportateur = new User();
            userImportateur.setEmail("importateur@test.ma");
            userImportateur.setPassword(passwordEncoder.encode("password"));
            userImportateur.setNom("Société Import Test");
            userImportateur.setTelephone("+212666123456");
            userImportateur.setTypeUser(TypeUser.IMPORTATEUR);
            userImportateur = userRepository.save(userImportateur);

            Importateur importateur = new Importateur();
            importateur.setUser(userImportateur);
            importateur.setRaisonSociale("Société Import Test");
            importateur.setAdresse("123 Rue du Commerce, Casablanca");
            importateur.setCodeDouane("CD123456");
            importateur.setIce("ICE123456789");
            importateurRepository.save(importateur);
            System.out.println("✅ Importateur de test créé: importateur@test.ma / password");
        }

        // Exportateur Demo (pour les tests uniquement)
        if (!userRepository.existsByEmail("exportateur@test.com")) {
            User userExportateur = new User();
            userExportateur.setEmail("exportateur@test.com");
            userExportateur.setPassword(passwordEncoder.encode("password"));
            userExportateur.setNom("Exportateur Test");
            userExportateur.setTelephone("+33123456789");
            userExportateur.setTypeUser(TypeUser.EXPORTATEUR);
            userExportateur = userRepository.save(userExportateur);

            Exportateur exportateur = new Exportateur();
            exportateur.setUser(userExportateur);
            exportateur.setRaisonSociale("Société Export Test");
            exportateur.setTelephone("+33123456789");
            exportateur.setEmail("exportateur@test.com");
            exportateur.setAdresse("456 Avenue Export, Lyon");
            exportateur.setPays("France");
            exportateur.setIfu("FR123456789");
            exportateurRepository.save(exportateur);
            System.out.println("✅ Exportateur de test créé: exportateur@test.com / password");
        }

        // Agent Demo (pour les tests uniquement)
        if (!userRepository.existsByEmail("agent1@tuv.ma")) {
            User userAgent = new User();
            userAgent.setEmail("agent1@tuv.ma");
            userAgent.setPassword(passwordEncoder.encode("password"));
            userAgent.setNom("Agent Test");
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

        // Superviseur Demo (pour les tests uniquement)
        if (!userRepository.existsByEmail("superviseur@tuv.ma")) {
            User userSuperviseur = new User();
            userSuperviseur.setEmail("superviseur@tuv.ma");
            userSuperviseur.setPassword(passwordEncoder.encode("password"));
            userSuperviseur.setNom("Superviseur Test");
            userSuperviseur.setTelephone("+212666345678");
            userSuperviseur.setTypeUser(TypeUser.SUPERVISEUR); // ✅ TYPE SUPERVISEUR
            userSuperviseur = userRepository.save(userSuperviseur);

            BureauControle tuv = bureauControleRepository.findByNom("TUV").orElse(null);
            if (tuv != null) {
                Superviseur superviseur = new Superviseur();
                superviseur.setUser(userSuperviseur);
                superviseur.setBureauControle(tuv);
                superviseur.setPeutReaffecter(true);
                superviseur.setPeutGererAgents(true);
                superviseur.setPeutVoirToutesLesDemandes(true);
                superviseur.setPeutTraiterDemandes(true);
                superviseur.setDisponiblePourTraitement(true);
                superviseur.setChargeTravailPersonnelle(0);
                superviseurRepository.save(superviseur);
                System.out.println("✅ Superviseur de test créé: superviseur@tuv.ma / password");
            }
        }
    }
}