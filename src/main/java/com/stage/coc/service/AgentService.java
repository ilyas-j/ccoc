package com.stage.coc.service;

import com.stage.coc.dto.request.AvisMarchandiseRequest;
import com.stage.coc.dto.response.DemandeResponse;
import com.stage.coc.dto.response.MarchandiseResponse;
import com.stage.coc.entity.*;
import com.stage.coc.enums.StatusDemande;
import com.stage.coc.exception.ResourceNotFoundException;
import com.stage.coc.exception.UnauthorizedException;
import com.stage.coc.repository.*;
import com.stage.coc.security.UserPrincipal;
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
public class AgentService {

    private final MarchandiseRepository marchandiseRepository;
    private final AvisMarchandiseRepository avisMarchandiseRepository;
    private final DemandeRepository demandeRepository;
    private final UserRepository userRepository;
    private final AgentRepository agentRepository;

    /**
     * Récupérer les demandes affectées à l'agent connecté
     */
    public List<DemandeResponse> getDemandesAffectees() {
        Agent agent = getAgentConnecte();

        List<Demande> demandes = demandeRepository.findByAgentId(agent.getId());
        return demandes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Prendre en charge une demande (DEPOSE -> EN_COURS_DE_TRAITEMENT)
     */
    public DemandeResponse prendreEnCharge(Long demandeId) {
        Agent agent = getAgentConnecte();

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        // Vérifier que la demande est affectée à cet agent
        if (!demande.getAgent().getId().equals(agent.getId())) {
            throw new UnauthorizedException("Cette demande n'est pas affectée à cet agent");
        }

        // Vérifier que le statut permet la prise en charge
        if (demande.getStatus() != StatusDemande.DEPOSE) {
            throw new IllegalStateException("Cette demande ne peut plus être prise en charge");
        }

        // Mettre à jour le statut
        demande.setStatus(StatusDemande.EN_COURS_DE_TRAITEMENT);
        demande.setDateTraitement(LocalDateTime.now());

        demande = demandeRepository.save(demande);
        return convertToResponse(demande);
    }

    /**
     * Donner un avis sur une marchandise
     */
    public void donnerAvisMarchandise(AvisMarchandiseRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Agent agent = user.getAgent();
        if (agent == null) {
            throw new RuntimeException("Agent non trouvé pour cet utilisateur");
        }

        Marchandise marchandise = marchandiseRepository.findById(request.getMarchandiseId())
                .orElseThrow(() -> new ResourceNotFoundException("Marchandise non trouvée"));

        // Vérifier que l'agent est bien affecté à cette demande
        if (!marchandise.getDemande().getAgent().getId().equals(agent.getId())) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à traiter cette marchandise");
        }

        // Vérifier que la demande est en cours de traitement
        if (marchandise.getDemande().getStatus() != StatusDemande.EN_COURS_DE_TRAITEMENT) {
            throw new IllegalStateException("La demande doit être en cours de traitement");
        }

        // Créer ou mettre à jour l'avis
        AvisMarchandise avis = avisMarchandiseRepository.findByMarchandiseId(request.getMarchandiseId())
                .orElse(new AvisMarchandise());

        avis.setAvis(request.getAvis());
        avis.setCommentaire(request.getCommentaire());
        avis.setMarchandise(marchandise);

        avisMarchandiseRepository.save(avis);

        // Vérifier si toutes les marchandises ont un avis pour auto-finalisation
        verifierEtCloturer(marchandise.getDemande());
    }

    /**
     * Finaliser manuellement un dossier
     */
    public DemandeResponse finaliserDossier(Long demandeId) {
        Agent agent = getAgentConnecte();

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        // Vérifications
        if (!demande.getAgent().getId().equals(agent.getId())) {
            throw new UnauthorizedException("Cette demande n'est pas affectée à cet agent");
        }

        if (demande.getStatus() != StatusDemande.EN_COURS_DE_TRAITEMENT) {
            throw new IllegalStateException("La demande doit être en cours de traitement");
        }

        // Vérifier que toutes les marchandises ont un avis
        List<Marchandise> marchandises = marchandiseRepository.findByDemandeId(demande.getId());
        boolean toutesTraitees = marchandises.stream()
                .allMatch(m -> avisMarchandiseRepository.findByMarchandiseId(m.getId()).isPresent());

        if (!toutesTraitees) {
            throw new IllegalStateException("Toutes les marchandises doivent avoir un avis");
        }

        finaliserDossier(demande);
        return convertToResponse(demande);
    }

    /**
     * Vérifier si toutes les marchandises ont un avis et finaliser automatiquement
     */
    private void verifierEtCloturer(Demande demande) {
        List<Marchandise> marchandises = marchandiseRepository.findByDemandeId(demande.getId());

        // Vérifier si toutes les marchandises ont un avis
        boolean toutesTraitees = marchandises.stream()
                .allMatch(m -> avisMarchandiseRepository.findByMarchandiseId(m.getId()).isPresent());

        if (toutesTraitees) {
            finaliserDossier(demande);
        }
    }

    /**
     * Finaliser le dossier avec calcul de la décision globale
     */
    private void finaliserDossier(Demande demande) {
        List<Marchandise> marchandises = marchandiseRepository.findByDemandeId(demande.getId());
        String decisionGlobale = calculerDecisionGlobale(marchandises);

        demande.setDecisionGlobale(decisionGlobale);
        demande.setStatus(StatusDemande.CLOTURE);
        demande.setDateCloture(LocalDateTime.now());

        // Décrémenter la charge de travail de l'agent
        Agent agent = demande.getAgent();
        agent.setChargeTravail(Math.max(0, agent.getChargeTravail() - 1));
        agentRepository.save(agent);

        demandeRepository.save(demande);
    }

    /**
     * Calculer la décision globale selon les règles du cahier des charges
     */
    private String calculerDecisionGlobale(List<Marchandise> marchandises) {
        boolean hasNonConforme = false;
        boolean hasConformeAvecReserve = false;

        for (Marchandise marchandise : marchandises) {
            AvisMarchandise avis = avisMarchandiseRepository.findByMarchandiseId(marchandise.getId())
                    .orElse(null);

            if (avis != null) {
                switch (avis.getAvis()) {
                    case NON_CONFORME:
                        hasNonConforme = true;
                        break;
                    case CONFORME_AVEC_RESERVE:
                        hasConformeAvecReserve = true;
                        break;
                    case CONFORME:
                        // Continue
                        break;
                }
            }
        }

        // Règles selon le cahier des charges
        if (hasNonConforme) {
            return "NON_CONFORME";
        } else if (hasConformeAvecReserve) {
            return "CONFORME_AVEC_RESERVE";
        } else {
            return "CONFORME";
        }
    }

    /**
     * Récupérer l'agent connecté
     */
    private Agent getAgentConnecte() {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Agent agent = user.getAgent();
        if (agent == null) {
            throw new ResourceNotFoundException("Agent non trouvé pour cet utilisateur");
        }

        return agent;
    }

    /**
     * Convertir Demande en DemandeResponse
     */
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

        // Convertir les marchandises avec leurs avis
        if (demande.getMarchandises() != null) {
            List<MarchandiseResponse> marchandises = demande.getMarchandises().stream()
                    .map(this::convertMarchandiseToResponse)
                    .collect(Collectors.toList());
            response.setMarchandises(marchandises);
        }

        return response;
    }

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
    // Ajouter cette méthode manquante
    public DemandeResponse getDemandeDetails(Long demandeId) {
        Agent agent = getAgentConnecte();

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        // Vérifier que la demande est affectée à cet agent
        if (!demande.getAgent().getId().equals(agent.getId())) {
            throw new UnauthorizedException("Cette demande n'est pas affectée à cet agent");
        }

        return convertToResponse(demande);
    }
}