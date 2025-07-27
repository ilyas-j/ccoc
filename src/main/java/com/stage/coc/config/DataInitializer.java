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
        createSystemAdmin();

        System.out.println("✅ Initialisation production terminée");
        System.out.println("🔐 Admin système: admin@portnet.ma / AdminCOC2024!");
        System.out.println("📝 Les utilisateurs peuvent maintenant s'inscrire via /register");    }

    private void createSystemAdmin() {
        if (!userRepository.existsByEmail("admin@portnet.ma")) {
            System.out.println("📋 Création de l'administrateur système...");

            // Créer l'utilisateur administrateur
            User adminUser = new User();
            adminUser.setEmail("admin@portnet.ma");
            adminUser.setPassword(passwordEncoder.encode("AdminCOC2024!"));
            adminUser.setNom("Administrateur Système COC");
            adminUser.setTelephone("+212666000000");
            adminUser.setTypeUser(TypeUser.SUPERVISEUR);
            adminUser = userRepository.save(adminUser);

            // Affecter au bureau TUV par défaut
            BureauControle tuv = bureauControleRepository.findByNom("TUV").orElse(null);
            if (tuv != null) {
                Superviseur superviseur = new Superviseur();
                superviseur.setUser(adminUser);
                superviseur.setBureauControle(tuv);
                superviseur.setPeutReaffecter(true);
                superviseur.setPeutGererAgents(true);
                superviseur.setPeutVoirToutesLesDemandes(true);
                superviseurRepository.save(superviseur);

                System.out.println("✅ Administrateur système créé avec succès");
                System.out.println("   Email: admin@portnet.ma");
                System.out.println("   Mot de passe: AdminCOC2024!");
                System.out.println("   Bureau: " + tuv.getNom());
            } else {
                System.err.println("❌ Erreur: Bureau TUV non trouvé pour l'admin");
            }
        } else {
            System.out.println("ℹ️ Administrateur système déjà existant");
        }
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
}