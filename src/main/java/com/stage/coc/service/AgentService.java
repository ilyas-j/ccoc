package com.stage.coc.service;

import com.stage.coc.dto.request.AvisMarchandiseRequest;
import com.stage.coc.entity.*;
import com.stage.coc.enums.StatusDemande;
import com.stage.coc.exception.ResourceNotFoundException;
import com.stage.coc.repository.*;
import com.stage.coc.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AgentService {

    private final MarchandiseRepository marchandiseRepository;
    private final AvisMarchandiseRepository avisMarchandiseRepository;
    private final DemandeRepository demandeRepository;
    private final UserRepository userRepository;

    public void donnerAvisMarchandise(AvisMarchandiseRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Marchandise marchandise = marchandiseRepository.findById(request.getMarchandiseId())
                .orElseThrow(() -> new ResourceNotFoundException("Marchandise non trouvée"));

        // Vérifier que l'agent est bien affecté à cette demande
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Agent agent = user.getAgent();
        if (agent == null || !marchandise.getDemande().getAgent().getId().equals(agent.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à traiter cette marchandise");
        }

        // Créer ou mettre à jour l'avis
        AvisMarchandise avis = avisMarchandiseRepository.findByMarchandiseId(request.getMarchandiseId())
                .orElse(new AvisMarchandise());

        avis.setAvis(request.getAvis());
        avis.setCommentaire(request.getCommentaire());
        avis.setMarchandise(marchandise);

        avisMarchandiseRepository.save(avis);

        // Vérifier si toutes les marchandises de la demande ont un avis
        verifierEtCloturer(marchandise.getDemande());
    }

    private void verifierEtCloturer(Demande demande) {
        List<Marchandise> marchandises = marchandiseRepository.findByDemandeId(demande.getId());

        // Vérifier si toutes les marchandises ont un avis
        boolean toutesTraitees = marchandises.stream()
                .allMatch(m -> avisMarchandiseRepository.findByMarchandiseId(m.getId()).isPresent());

        if (toutesTraitees) {
            // Calculer la décision globale
            String decisionGlobale = calculerDecisionGlobale(marchandises);

            demande.setDecisionGlobale(decisionGlobale);
            demande.setStatus(StatusDemande.CLOTURE);
            demande.setDateCloture(LocalDateTime.now());

            demandeRepository.save(demande);
        }
    }

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
                }
            }
        }

        if (hasNonConforme) {
            return "NON_CONFORME";
        } else if (hasConformeAvecReserve) {
            return "CONFORME_AVEC_RESERVE";
        } else {
            return "CONFORME";
        }
    }
}
