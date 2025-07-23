package com.stage.coc.config;

import com.stage.coc.entity.*;
import com.stage.coc.enums.CategorieMarchandise;
import com.stage.coc.enums.TypeUser;
import com.stage.coc.enums.UniteQuantite;
import com.stage.coc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@DependsOn({"userRepository", "importateurRepository", "exportateurRepository"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ImportateurRepository importateurRepository;
    private final ExportateurRepository exportateurRepository; // ✅ AJOUTER
    private final BureauControleRepository bureauControleRepository;
    private final AgentRepository agentRepository;
    private final DemandeRepository demandeRepository; // ✅ AJOUTER
    private final MarchandiseRepository marchandiseRepository; // ✅ AJOUTER
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initBureauxControle();
        initUtilisateursDemo();
        initDemandesDemo(); // ✅ AJOUTER
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
        // Importateur Demo (code existant)
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

        // ✅ AJOUTER : Exportateurs Demo
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
        }

        if (!userRepository.existsByEmail("exportateur2@test.de")) {
            User userExportateur2 = new User();
            userExportateur2.setEmail("exportateur2@test.de");
            userExportateur2.setPassword(passwordEncoder.encode("password"));
            userExportateur2.setNom("Exportateur Allemagne");
            userExportateur2.setTelephone("+49123456789");
            userExportateur2.setTypeUser(TypeUser.EXPORTATEUR);
            userExportateur2 = userRepository.save(userExportateur2);

            Exportateur exportateur2 = new Exportateur();
            exportateur2.setUser(userExportateur2);
            exportateur2.setRaisonSociale("German Export GmbH");
            exportateur2.setTelephone("+49123456789");
            exportateur2.setEmail("exportateur2@test.de");
            exportateur2.setAdresse("123 Export Strasse, Berlin");
            exportateur2.setPays("Allemagne");
            exportateur2.setIfu("DE987654321");
            exportateurRepository.save(exportateur2);
        }

        // Agent Demo (code existant)
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

        // Superviseur Demo (code existant)
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

    // ✅ AJOUTER : Méthode pour créer des demandes de démonstration
    private void initDemandesDemo() {
        // Vérifier si des demandes existent déjà
        if (demandeRepository.count() > 0) {
            return; // Des demandes existent déjà
        }

        try {
            // Récupérer les entités nécessaires
            User importateurUser = userRepository.findByEmail("importateur@test.ma").orElse(null);
            User exportateurUser = userRepository.findByEmail("exportateur@test.com").orElse(null);
            User agentUser = userRepository.findByEmail("agent1@tuv.ma").orElse(null);
            BureauControle tuv = bureauControleRepository.findByNom("TUV").orElse(null);

            if (importateurUser == null || exportateurUser == null || agentUser == null || tuv == null) {
                System.out.println("⚠️ Impossible de créer les demandes de démo - Entités manquantes");
                return;
            }

            Importateur importateur = importateurRepository.findByUserId(importateurUser.getId()).orElse(null);
            Exportateur exportateur = exportateurRepository.findByUserId(exportateurUser.getId()).orElse(null);
            Agent agent = agentRepository.findByUserId(agentUser.getId()).orElse(null);

            if (importateur == null || exportateur == null || agent == null) {
                System.out.println("⚠️ Profils importateur/exportateur/agent manquants");
                return;
            }

            // Créer une demande de démonstration
            Demande demande = new Demande();
            demande.setImportateur(importateur);
            demande.setExportateur(exportateur);
            demande.setBureauControle(tuv);
            demande.setAgent(agent);
            demande = demandeRepository.save(demande);

            // Ajouter des marchandises de démonstration
            Marchandise marchandise1 = new Marchandise();
            marchandise1.setCategorie(CategorieMarchandise.EQUIPEMENTS_ECLAIRAGE);
            marchandise1.setQuantite(new BigDecimal("100"));
            marchandise1.setUniteQuantite(UniteQuantite.PIECE);
            marchandise1.setValeurDh(new BigDecimal("15000"));
            marchandise1.setNomProduit("Lampe LED");
            marchandise1.setFabricant("LightTech SA");
            marchandise1.setAdresseFabricant("123 Rue de la Lumière, Lyon");
            marchandise1.setPaysOrigine("France");
            marchandise1.setDemande(demande);
            marchandiseRepository.save(marchandise1);

            Marchandise marchandise2 = new Marchandise();
            marchandise2.setCategorie(CategorieMarchandise.JOUETS_ET_ARTICLES_ENFANTS);
            marchandise2.setQuantite(new BigDecimal("50"));
            marchandise2.setUniteQuantite(UniteQuantite.PIECE);
            marchandise2.setValeurDh(new BigDecimal("8500"));
            marchandise2.setNomProduit("Jouet Robot");
            marchandise2.setFabricant("ToyMaker Ltd");
            marchandise2.setAdresseFabricant("456 Kids Street, Paris");
            marchandise2.setPaysOrigine("France");
            marchandise2.setDemande(demande);
            marchandiseRepository.save(marchandise2);

            System.out.println("✅ Demande de démonstration créée : " + demande.getNumeroDemande());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création des demandes de démo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}