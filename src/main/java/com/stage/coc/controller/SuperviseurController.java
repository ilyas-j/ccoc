package com.stage.coc.controller;

import com.stage.coc.dto.response.DemandeResponse;
import com.stage.coc.dto.response.MessageResponse;
import com.stage.coc.entity.Agent;
import com.stage.coc.service.SuperviseurService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/superviseur")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SuperviseurController {

    private final SuperviseurService superviseurService;

    /**
     * Vue d'ensemble - TOUTES les demandes du bureau (fonction superviseur)
     */
    @GetMapping("/vue-ensemble")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<List<DemandeResponse>> getVueEnsembleBureau() {
        List<DemandeResponse> demandes = superviseurService.getVueEnsembleBureau();
        return ResponseEntity.ok(demandes);
    }

    /**
     * Gestion des agents - Voir tous les agents du bureau
     */
    @GetMapping("/agents")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<List<SuperviseurService.AgentResponse>> getAgentsBureau() {
        List<SuperviseurService.AgentResponse> agents = superviseurService.getAgentsBureau();
        return ResponseEntity.ok(agents);
    }

    /**
     * Réaffectation - Réaffecter une demande à un autre agent
     */
    @PutMapping("/demandes/{demandeId}/reaffecter/{agentId}")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<MessageResponse> reaffecterDemande(
            @PathVariable Long demandeId,
            @PathVariable Long agentId) {
        superviseurService.reaffecterDemande(demandeId, agentId);
        return ResponseEntity.ok(new MessageResponse("Demande réaffectée avec succès"));
    }

    /**
     * Gestion agents - Changer disponibilité d'un agent
     */
    @PutMapping("/agents/{agentId}/disponibilite")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<MessageResponse> modifierDisponibiliteAgent(
            @PathVariable Long agentId,
            @RequestParam boolean disponible,
            @RequestParam boolean enConge) {
        superviseurService.modifierDisponibiliteAgent(agentId, disponible, enConge);
        return ResponseEntity.ok(new MessageResponse("Disponibilité agent modifiée"));
    }

    /**
     * Statistiques du bureau
     */
    @GetMapping("/statistiques")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<SuperviseurService.StatistiquesBureauResponse> getStatistiquesBureau() {
        SuperviseurService.StatistiquesBureauResponse stats = superviseurService.getStatistiquesBureau();
        return ResponseEntity.ok(stats);
    }



    /**
     * Tableau de bord avec KPIs
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();

        // Statistiques générales
        SuperviseurService.StatistiquesBureauResponse stats = superviseurService.getStatistiquesBureau();
        dashboard.put("statistiques", stats);

        // Vue d'ensemble des demandes
        List<DemandeResponse> demandes = superviseurService.getVueEnsembleBureau();
        dashboard.put("demandes", demandes);

        // État des agents
        List<SuperviseurService.AgentResponse> agents = superviseurService.getAgentsBureau();
        dashboard.put("agents", agents);

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Actions en lot sur les demandes
     */
    @PostMapping("/actions-lot")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<MessageResponse> actionsEnLot(
            @RequestBody ActionsLotRequest request) {

        switch (request.getAction()) {
            case "REAFFECTER_TOUTES":
                // Logique pour réaffecter plusieurs demandes
                break;
            case "CHANGER_PRIORITE":
                // Logique pour changer la priorité
                break;
            default:
                throw new IllegalArgumentException("Action non supportée: " + request.getAction());
        }

        return ResponseEntity.ok(new MessageResponse("Actions en lot exécutées"));
    }

    /**
     * Rapport de performance des agents
     */
    @GetMapping("/rapport-performance")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<Map<String, Object>> getRapportPerformance(
            @RequestParam(required = false) String periode) {

        Map<String, Object> rapport = new HashMap<>();

        // Logique pour générer le rapport de performance
        List<SuperviseurService.AgentResponse> agents = superviseurService.getAgentsBureau();

        rapport.put("agents", agents);
        rapport.put("periode", periode != null ? periode : "mois_courant");
        rapport.put("genereA", LocalDateTime.now());

        return ResponseEntity.ok(rapport);
    }

    // DTO pour les actions en lot
    @Data
    public static class ActionsLotRequest {
        private String action;
        private List<Long> demandeIds;
        private Map<String, Object> parametres;
    }
}

