package com.stage.coc.service;

import com.stage.coc.dto.response.DemandeResponse;
import com.stage.coc.dto.response.MarchandiseResponse;
import com.stage.coc.entity.*;
import com.stage.coc.enums.StatusDemande;
import com.stage.coc.enums.TypeUser;
import com.stage.coc.exception.ResourceNotFoundException;
import com.stage.coc.exception.UnauthorizedException;
import com.stage.coc.repository.*;
import com.stage.coc.security.UserPrincipal;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SuperviseurService {

    private final DemandeRepository demandeRepository;
    private final AgentRepository agentRepository;
    private final SuperviseurRepository superviseurRepository;
    private final UserRepository userRepository;
    private final AffectationService affectationService;
    private final AvisMarchandiseRepository avisMarchandiseRepository;

    /**
     * Récupérer le superviseur connecté
     */
    private Superviseur getSuperviseurConnecte() {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (user.getTypeUser() != TypeUser.SUPERVISEUR) {
            throw new UnauthorizedException("Accès réservé aux superviseurs");
        }

        return superviseurRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil superviseur non trouvé"));
    }

    /**
     * 🎯 FONCTION SUPERVISEUR PRINCIPALE: Vue d'ensemble de TOUTES les demandes du bureau
     */
    public List<DemandeResponse> getVueEnsembleBureau() {
        Superviseur superviseur = getSuperviseurConnecte();

        System.out.println("🏢 Superviseur " + superviseur.getUser().getNom() +
                " récupère toutes les demandes du bureau " + superviseur.getBureauControle().getNom());

        if (!superviseur.isPeutVoirToutesLesDemandes()) {
            throw new UnauthorizedException("Permission insuffisante pour voir toutes les demandes");
        }

        List<Demande> demandes = demandeRepository.findByBureauControleId(
                superviseur.getBureauControle().getId()
        );

        System.out.println("✅ " + demandes.size() + " demandes trouvées pour le bureau " +
                superviseur.getBureauControle().getNom());

        return demandes.stream()
                .map(this::convertToDemandeResponse)
                .collect(Collectors.toList());
    }

    /**
     * 🎯 FONCTION SUPERVISEUR: Gérer tous les agents du bureau
     */
    public List<AgentResponse> getAgentsBureau() {
        Superviseur superviseur = getSuperviseurConnecte();

        System.out.println("👥 Superviseur récupère tous les agents du bureau " +
                superviseur.getBureauControle().getNom());

        if (!superviseur.isPeutGererAgents()) {
            throw new UnauthorizedException("Permission insuffisante pour gérer les agents");
        }

        List<Agent> agents = agentRepository.findByBureauControleId(
                superviseur.getBureauControle().getId()
        );

        System.out.println("✅ " + agents.size() + " agents trouvés dans le bureau");

        return agents.stream()
                .map(this::convertToAgentResponse)
                .collect(Collectors.toList());
    }

    /**
     * 🎯 FONCTION SUPERVISEUR CRITIQUE: Réaffecter une demande à un autre agent
     */
    public void reaffecterDemande(Long demandeId, Long nouvelAgentId) {
        Superviseur superviseur = getSuperviseurConnecte();

        System.out.println("🔄 Superviseur " + superviseur.getUser().getNom() +
                " réaffecte la demande " + demandeId + " à l'agent " + nouvelAgentId);

        if (!superviseur.isPeutReaffecter()) {
            throw new UnauthorizedException("Permission insuffisante pour réaffecter");
        }

        // Vérifier que la demande appartient au bureau du superviseur
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        if (!demande.getBureauControle().getId().equals(superviseur.getBureauControle().getId())) {
            throw new UnauthorizedException("Vous ne pouvez réaffecter que les demandes de votre bureau");
        }

        // Vérifier que le nouvel agent appartient au bureau
        Agent nouvelAgent = agentRepository.findById(nouvelAgentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent non trouvé"));

        if (!nouvelAgent.getBureauControle().getId().equals(superviseur.getBureauControle().getId())) {
            throw new UnauthorizedException("L'agent doit appartenir au même bureau");
        }

        // Vérifier que l'agent est disponible
        if (!nouvelAgent.isDisponible() || nouvelAgent.isEnConge()) {
            throw new IllegalStateException("L'agent sélectionné n'est pas disponible");
        }

        // Effectuer la réaffectation
        affectationService.reaffecter(demandeId, nouvelAgentId);

        // Mettre à jour la date d'affectation
        demande.setDateTraitement(LocalDateTime.now());
        demandeRepository.save(demande);

        System.out.println("✅ Demande " + demande.getNumeroDemande() +
                " réaffectée à " + nouvelAgent.getUser().getNom());
    }

    /**
     * 🎯 FONCTION SUPERVISEUR: Modifier la disponibilité d'un agent
     */
    public void modifierDisponibiliteAgent(Long agentId, boolean disponible, boolean enConge) {
        Superviseur superviseur = getSuperviseurConnecte();

        System.out.println("👤 Superviseur modifie la disponibilité de l'agent " + agentId +
                " - Disponible: " + disponible + ", En congé: " + enConge);

        if (!superviseur.isPeutGererAgents()) {
            throw new UnauthorizedException("Permission insuffisante pour gérer les agents");
        }

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent non trouvé"));

        // Vérifier que l'agent appartient au même bureau
        if (!agent.getBureauControle().getId().equals(superviseur.getBureauControle().getId())) {
            throw new UnauthorizedException("Vous ne pouvez gérer que les agents de votre bureau");
        }

        // Si l'agent passe en congé, réaffecter ses demandes en cours
        if (enConge && !agent.isEnConge()) {
            reassignerDemandesAgent(agent);
        }

        agent.setDisponible(disponible);
        agent.setEnConge(enConge);

        // Si en congé, mettre la charge à 0
        if (enConge) {
            agent.setChargeTravail(0);
        }

        agentRepository.save(agent);
        System.out.println("✅ Disponibilité agent modifiée");
    }

    /**
     * 🎯 FONCTION SUPERVISEUR: Obtenir les statistiques du bureau
     */
    public StatistiquesBureauResponse getStatistiquesBureau() {
        Superviseur superviseur = getSuperviseurConnecte();
        Long bureauId = superviseur.getBureauControle().getId();

        System.out.println("📊 Calcul des statistiques pour le bureau " + superviseur.getBureauControle().getNom());

        List<Demande> demandes = demandeRepository.findByBureauControleId(bureauId);
        List<Agent> agents = agentRepository.findByBureauControleId(bureauId);

        StatistiquesBureauResponse stats = new StatistiquesBureauResponse();
        stats.setTotalDemandes(demandes.size());
        stats.setDemandesDeposees(demandes.stream()
                .mapToInt(d -> d.getStatus() == StatusDemande.DEPOSE ? 1 : 0).sum());
        stats.setDemandesEnCours(demandes.stream()
                .mapToInt(d -> d.getStatus() == StatusDemande.EN_COURS_DE_TRAITEMENT ? 1 : 0).sum());
        stats.setDemandesCloses(demandes.stream()
                .mapToInt(d -> d.getStatus() == StatusDemande.CLOTURE ? 1 : 0).sum());

        stats.setTotalAgents(agents.size());
        stats.setAgentsDisponibles(agents.stream()
                .mapToInt(a -> (a.isDisponible() && !a.isEnConge()) ? 1 : 0).sum());
        stats.setAgentsEnConge(agents.stream()
                .mapToInt(a -> a.isEnConge() ? 1 : 0).sum());

        stats.setChargeGlobale(agents.stream()
                .mapToInt(Agent::getChargeTravail).sum());

        // Calculer le temps de traitement moyen (simulation)
        stats.setTempsTraitementMoyen(calculateTempsTraitementMoyen(demandes));

        // Calculer le taux de conformité
        stats.setTauxConformite(calculateTauxConformite(demandes));

        System.out.println("✅ Statistiques calculées: " + stats.getTotalDemandes() + " demandes, " +
                stats.getTotalAgents() + " agents");

        return stats;
    }



    /**
     * 🔄 FONCTION CRITIQUE: Réassigner automatiquement les demandes d'un agent en congé
     */
    private void reassignerDemandesAgent(Agent agent) {
        System.out.println("🔄 Réassignation automatique des demandes de l'agent en congé: " +
                agent.getUser().getNom());

        List<Demande> demandesEnCours = demandeRepository
                .findByAgentIdAndStatus(agent.getId(), StatusDemande.EN_COURS_DE_TRAITEMENT);

        List<Demande> demandesDeposees = demandeRepository
                .findByAgentIdAndStatus(agent.getId(), StatusDemande.DEPOSE);

        List<Agent> agentsDisponibles = agentRepository
                .findAvailableAgentsByBureauControleOrderByWorkload(agent.getBureauControle().getId());

        // Exclure l'agent qui va en congé
        agentsDisponibles = agentsDisponibles.stream()
                .filter(a -> !a.getId().equals(agent.getId()))
                .collect(Collectors.toList());

        if (agentsDisponibles.isEmpty()) {
            System.out.println("⚠️ Aucun agent disponible pour la réassignation");
            return;
        }

        // Répartir les demandes entre les agents disponibles
        int agentIndex = 0;
        List<Demande> toutesLesDemandesAReaffecter = demandesEnCours;
        toutesLesDemandesAReaffecter.addAll(demandesDeposees);

        for (Demande demande : toutesLesDemandesAReaffecter) {
            Agent nouvelAgent = agentsDisponibles.get(agentIndex % agentsDisponibles.size());
            affectationService.reaffecter(demande.getId(), nouvelAgent.getId());
            agentIndex++;

            System.out.println("  → Demande " + demande.getNumeroDemande() +
                    " réaffectée à " + nouvelAgent.getUser().getNom());
        }

        System.out.println("✅ " + toutesLesDemandesAReaffecter.size() + " demandes réaffectées");
    }

    /**
     * 📊 Calculer le temps de traitement moyen
     */
    private double calculateTempsTraitementMoyen(List<Demande> demandes) {
        List<Demande> demandesCloses = demandes.stream()
                .filter(d -> d.getStatus() == StatusDemande.CLOTURE &&
                        d.getDateCreation() != null && d.getDateCloture() != null)
                .collect(Collectors.toList());

        if (demandesCloses.isEmpty()) {
            return 2.5; // Valeur par défaut
        }

        double totalJours = demandesCloses.stream()
                .mapToDouble(d -> {
                    long heures = java.time.Duration.between(d.getDateCreation(), d.getDateCloture()).toHours();
                    return heures / 24.0; // Convertir en jours
                })
                .sum();

        return totalJours / demandesCloses.size();
    }

    /**
     * 📊 Calculer le taux de conformité
     */
    private double calculateTauxConformite(List<Demande> demandes) {
        List<Demande> demandesAvecDecision = demandes.stream()
                .filter(d -> d.getDecisionGlobale() != null)
                .collect(Collectors.toList());

        if (demandesAvecDecision.isEmpty()) {
            return 85.0; // Valeur par défaut
        }

        long conformes = demandesAvecDecision.stream()
                .mapToLong(d -> "CONFORME".equals(d.getDecisionGlobale()) ? 1 : 0)
                .sum();

        return (conformes * 100.0) / demandesAvecDecision.size();
    }

    // =====================================
    // 📦 CLASSES DE RÉPONSE
    // =====================================

    @Data
    public static class StatistiquesBureauResponse {
        private int totalDemandes;
        private int demandesDeposees;
        private int demandesEnCours;
        private int demandesCloses;
        private int totalAgents;
        private int agentsDisponibles;
        private int agentsEnConge;
        private int chargeGlobale;
        private double tempsTraitementMoyen;
        private double tauxConformite;
    }

    @Data
    public static class AgentResponse {
        private Long id;
        private String nom;
        private String email;
        private String telephone;
        private boolean disponible;
        private boolean enConge;
        private int chargeTravail;
        private int demandesTraitees;
        private double tauxConformite;
        private boolean superviseur;
    }

    // =====================================
    // 🔄 MÉTHODES DE CONVERSION
    // =====================================

    /**
     * Convertir Demande → DemandeResponse (avec toutes les informations)
     */
    private DemandeResponse convertToDemandeResponse(Demande demande) {
        DemandeResponse response = new DemandeResponse();
        response.setId(demande.getId());
        response.setNumeroDemande(demande.getNumeroDemande());
        response.setStatus(demande.getStatus());
        response.setDateCreation(demande.getDateCreation());
        response.setDateTraitement(demande.getDateTraitement());
        response.setDateCloture(demande.getDateCloture());
        response.setDecisionGlobale(demande.getDecisionGlobale());
        response.setDateAffectation(demande.getDateCreation()); // Pour l'instant = date création
        response.setDelaiEstime(calculerDelaiEstime(demande));

        // Informations importateur
        if (demande.getImportateur() != null) {
            response.setImportateurNom(demande.getImportateur().getRaisonSociale());
        }

        // Informations exportateur
        if (demande.getExportateur() != null) {
            response.setExportateurNom(demande.getExportateur().getRaisonSociale());
        }

        // Informations bureau de contrôle
        if (demande.getBureauControle() != null) {
            response.setBureauControleNom(demande.getBureauControle().getNom());
        }

        // Informations agent affecté
        if (demande.getAgent() != null && demande.getAgent().getUser() != null) {
            response.setAgentNom(demande.getAgent().getUser().getNom());
        }

        // Marchandises avec leurs avis
        if (demande.getMarchandises() != null) {
            List<MarchandiseResponse> marchandises = demande.getMarchandises().stream()
                    .map(this::convertMarchandiseToResponse)
                    .collect(Collectors.toList());
            response.setMarchandises(marchandises);
        }

        return response;
    }

    /**
     * Convertir Agent → AgentResponse
     */
    private AgentResponse convertToAgentResponse(Agent agent) {
        AgentResponse response = new AgentResponse();
        response.setId(agent.getId());
        response.setNom(agent.getUser().getNom());
        response.setEmail(agent.getUser().getEmail());
        response.setTelephone(agent.getUser().getTelephone());
        response.setDisponible(agent.isDisponible());
        response.setEnConge(agent.isEnConge());
        response.setChargeTravail(agent.getChargeTravail());
        response.setSuperviseur(agent.isSuperviseur());

        // Calculer les statistiques de performance
        response.setDemandesTraitees(calculateDemandesTraitees(agent));
        response.setTauxConformite(calculateTauxConformiteAgent(agent));

        return response;
    }

    /**
     * Convertir Marchandise → MarchandiseResponse
     */
    private MarchandiseResponse convertMarchandiseToResponse(Marchandise marchandise) {
        MarchandiseResponse response = new MarchandiseResponse();
        response.setId(marchandise.getId());
        response.setCategorie(marchandise.getCategorie());
        response.setQuantite(marchandise.getQuantite());
        response.setUniteQuantite(marchandise.getUniteQuantite());
        response.setValeurDh(marchandise.getValeurDh());
        response.setNomProduit(marchandise.getNomProduit());
        response.setFabricant(marchandise.getFabricant());
        response.setAdresseFabricant(marchandise.getAdresseFabricant());
        response.setPaysOrigine(marchandise.getPaysOrigine());

        // Ajouter l'avis si disponible
        avisMarchandiseRepository.findByMarchandiseId(marchandise.getId())
                .ifPresent(avis -> {
                    response.setAvis(avis.getAvis().toString());
                    response.setCommentaire(avis.getCommentaire());
                });

        return response;
    }

    /**
     * Calculer le délai estimé
     */
    private String calculerDelaiEstime(Demande demande) {
        if (demande.getMarchandises() != null) {
            int nombreMarchandises = demande.getMarchandises().size();
            int delai = Math.max(1, nombreMarchandises / 2 + 1);
            return delai + " jour(s)";
        }
        return "2 jour(s)";
    }

    /**
     * Calculer le nombre de demandes traitées par un agent
     */
    private int calculateDemandesTraitees(Agent agent) {
        return demandeRepository.findByAgentIdAndStatus(agent.getId(), StatusDemande.CLOTURE).size();
    }

    /**
     * Calculer le taux de conformité d'un agent
     */
    private double calculateTauxConformiteAgent(Agent agent) {
        List<Demande> demandesCloses = demandeRepository
                .findByAgentIdAndStatus(agent.getId(), StatusDemande.CLOTURE);

        if (demandesCloses.isEmpty()) {
            return 90.0; // Valeur par défaut
        }

        long conformes = demandesCloses.stream()
                .mapToLong(d -> "CONFORME".equals(d.getDecisionGlobale()) ? 1 : 0)
                .sum();

        return (conformes * 100.0) / demandesCloses.size();
    }
}