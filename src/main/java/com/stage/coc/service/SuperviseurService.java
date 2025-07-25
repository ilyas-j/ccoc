package com.stage.coc.service;

import com.stage.coc.dto.response.DemandeResponse;
import com.stage.coc.entity.Agent;
import com.stage.coc.entity.Demande;
import com.stage.coc.entity.Superviseur;
import com.stage.coc.entity.User;
import com.stage.coc.enums.StatusDemande;
import com.stage.coc.enums.TypeUser;
import com.stage.coc.exception.ResourceNotFoundException;
import com.stage.coc.exception.UnauthorizedException;
import com.stage.coc.repository.AgentRepository;
import com.stage.coc.repository.DemandeRepository;
import com.stage.coc.repository.SuperviseurRepository;
import com.stage.coc.repository.UserRepository;
import com.stage.coc.security.UserPrincipal;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * FONCTION SUPERVISEUR: Vue d'ensemble de TOUTES les demandes du bureau
     */
    public List<DemandeResponse> getVueEnsembleBureau() {
        Superviseur superviseur = getSuperviseurConnecte();

        if (!superviseur.isPeutVoirToutesLesDemandes()) {
            throw new UnauthorizedException("Permission insuffisante pour voir toutes les demandes");
        }

        List<Demande> demandes = demandeRepository.findByBureauControleId(
                superviseur.getBureauControle().getId()
        );

        return demandes.stream()
                .map(this::convertToDemandeResponse)
                .collect(Collectors.toList());
    }

    /**
     * FONCTION SUPERVISEUR: Gérer tous les agents du bureau
     */
    public List<AgentResponse> getAgentsBureau() {
        Superviseur superviseur = getSuperviseurConnecte();

        if (!superviseur.isPeutGererAgents()) {
            throw new UnauthorizedException("Permission insuffisante pour gérer les agents");
        }

        List<Agent> agents = agentRepository.findByBureauControleId(
                superviseur.getBureauControle().getId()
        );

        return agents.stream()
                .map(this::convertToAgentResponse)
                .collect(Collectors.toList());
    }

    /**
     * FONCTION SUPERVISEUR: Réaffecter une demande à un autre agent
     */
    public void reaffecterDemande(Long demandeId, Long nouvelAgentId) {
        Superviseur superviseur = getSuperviseurConnecte();

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

        affectationService.reaffecter(demandeId, nouvelAgentId);
    }

    /**
     * FONCTION SUPERVISEUR: Modifier la disponibilité d'un agent
     */
    public void modifierDisponibiliteAgent(Long agentId, boolean disponible, boolean enConge) {
        Superviseur superviseur = getSuperviseurConnecte();

        if (!superviseur.isPeutGererAgents()) {
            throw new UnauthorizedException("Permission insuffisante pour gérer les agents");
        }

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent non trouvé"));

        // Vérifier que l'agent appartient au même bureau
        if (!agent.getBureauControle().getId().equals(superviseur.getBureauControle().getId())) {
            throw new UnauthorizedException("Vous ne pouvez gérer que les agents de votre bureau");
        }

        agent.setDisponible(disponible);
        agent.setEnConge(enConge);

        // Si en congé, réaffecter ses demandes en cours
        if (enConge && !agent.isEnConge()) {
            reassignerDemandesAgent(agent);
        }

        agentRepository.save(agent);
    }

    /**
     * FONCTION SUPERVISEUR: Obtenir les statistiques du bureau
     */
    public StatistiquesBureauResponse getStatistiquesBureau() {
        Superviseur superviseur = getSuperviseurConnecte();
        Long bureauId = superviseur.getBureauControle().getId();

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

        return stats;
    }

    /**
     * FONCTION SUPERVISEUR: Traiter personnellement une demande (comme un agent)
     */
    public List<DemandeResponse> getMesDemandesPersonnelles() {
        Superviseur superviseur = getSuperviseurConnecte();

        if (!superviseur.isPeutTraiterDemandes()) {
            throw new UnauthorizedException("Traitement personnel non autorisé");
        }

        // Récupérer les demandes affectées directement au superviseur
        List<Demande> demandes = demandeRepository.findByAgentUserId(superviseur.getUser().getId());

        return demandes.stream()
                .map(this::convertToDemandeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Réassigner automatiquement les demandes d'un agent en congé
     */
    private void reassignerDemandesAgent(Agent agent) {
        List<Demande> demandesEnCours = demandeRepository
                .findByAgentIdAndStatus(agent.getId(), StatusDemande.EN_COURS_DE_TRAITEMENT);

        List<Agent> agentsDisponibles = agentRepository
                .findAvailableAgentsByBureauControleOrderByWorkload(agent.getBureauControle().getId());

        // Répartir les demandes entre les agents disponibles
        int agentIndex = 0;
        for (Demande demande : demandesEnCours) {
            if (!agentsDisponibles.isEmpty()) {
                Agent nouvelAgent = agentsDisponibles.get(agentIndex % agentsDisponibles.size());
                affectationService.reaffecter(demande.getId(), nouvelAgent.getId());
                agentIndex++;
            }
        }
    }

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
        private boolean disponible;
        private boolean enConge;
        private int chargeTravail;
        private int demandesTraitees;
        private double tauxConformite;
    }

    // Méthodes de conversion
    private DemandeResponse convertToDemandeResponse(Demande demande) {
        // Implementation similaire à celle existante
        DemandeResponse response = new DemandeResponse();
        // ... mapping des champs
        return response;
    }

    private AgentResponse convertToAgentResponse(Agent agent) {
        AgentResponse response = new AgentResponse();
        response.setId(agent.getId());
        response.setNom(agent.getUser().getNom());
        response.setEmail(agent.getUser().getEmail());
        response.setDisponible(agent.isDisponible());
        response.setEnConge(agent.isEnConge());
        response.setChargeTravail(agent.getChargeTravail());
        // Calculer statistiques supplémentaires si nécessaire
        return response;
    }
}

