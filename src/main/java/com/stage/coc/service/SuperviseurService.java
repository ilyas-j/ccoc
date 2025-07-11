package com.stage.coc.service;

import com.stage.coc.dto.response.DemandeResponse;
import com.stage.coc.entity.Agent;
import com.stage.coc.entity.Demande;
import com.stage.coc.entity.User;
import com.stage.coc.exception.ResourceNotFoundException;
import com.stage.coc.repository.AgentRepository;
import com.stage.coc.repository.DemandeRepository;
import com.stage.coc.repository.UserRepository;
import com.stage.coc.security.UserPrincipal;
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
    private final UserRepository userRepository;
    private final AffectationService affectationService;

    public List<DemandeResponse> getDemandesBureau() {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Agent superviseur = user.getAgent();
        if (superviseur == null || !superviseur.isSuperviseur()) {
            throw new RuntimeException("Accès non autorisé");
        }

        List<Demande> demandes = demandeRepository.findByBureauControleId(superviseur.getBureauControle().getId());
        return demandes.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public void reaffecterDemande(Long demandeId, Long nouvelAgentId) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Agent superviseur = user.getAgent();
        if (superviseur == null || !superviseur.isSuperviseur()) {
            throw new RuntimeException("Seuls les superviseurs peuvent réaffecter les demandes");
        }

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        // Vérifier que la demande appartient au bureau du superviseur
        if (!demande.getBureauControle().getId().equals(superviseur.getBureauControle().getId())) {
            throw new RuntimeException("Vous ne pouvez réaffecter que les demandes de votre bureau");
        }

        affectationService.reaffecter(demandeId, nouvelAgentId);
    }

    public List<Agent> getAgentsBureau() {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Agent superviseur = user.getAgent();
        if (superviseur == null || !superviseur.isSuperviseur()) {
            throw new RuntimeException("Accès non autorisé");
        }

        return agentRepository.findByBureauControleId(superviseur.getBureauControle().getId());
    }

    private DemandeResponse convertToResponse(Demande demande) {
        DemandeResponse response = new DemandeResponse();
        response.setId(demande.getId());
        response.setNumeroDemande(demande.getNumeroDemande());
        response.setStatus(demande.getStatus());
        response.setDateCreation(demande.getDateCreation());
        response.setDateTraitement(demande.getDateTraitement());
        response.setDateCloture(demande.getDateCloture());
        response.setDecisionGlobale(demande.getDecisionGlobale());

        if (demande.getImportateur() != null) {
            response.setImportateurNom(demande.getImportateur().getRaisonSociale());
        }

        if (demande.getExportateur() != null) {
            response.setExportateurNom(demande.getExportateur().getRaisonSociale());
        }

        if (demande.getBureauControle() != null) {
            response.setBureauControleNom(demande.getBureauControle().getNom());
        }

        if (demande.getAgent() != null && demande.getAgent().getUser() != null) {
            response.setAgentNom(demande.getAgent().getUser().getNom());
        }

        return response;
    }
}

