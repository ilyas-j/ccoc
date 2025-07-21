package com.stage.coc.controller;

import com.stage.coc.dto.request.DemandeRequest;
import com.stage.coc.dto.response.DemandeResponse;
<<<<<<< HEAD
import com.stage.coc.entity.Agent;
import com.stage.coc.entity.Demande;
import com.stage.coc.entity.Marchandise;
import com.stage.coc.entity.User;
import com.stage.coc.enums.StatusDemande;
import com.stage.coc.exception.ResourceNotFoundException;
import com.stage.coc.repository.DemandeRepository;
import com.stage.coc.repository.MarchandiseRepository;
import com.stage.coc.repository.UserRepository;
import com.stage.coc.security.UserPrincipal; // CORRECTION 1: Import correct
=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
import com.stage.coc.service.DemandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
<<<<<<< HEAD
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
=======
import org.springframework.web.bind.annotation.*;

import java.util.List;
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DemandeController {

    private final DemandeService demandeService;
<<<<<<< HEAD
    private final DemandeRepository demandeRepository;
    private final UserRepository userRepository;
    private final MarchandiseRepository marchandiseRepository;

    @PostMapping
    @PreAuthorize("hasRole('IMPORTATEUR') or hasRole('EXPORTATEUR')")
=======

    @PostMapping
    @PreAuthorize("hasRole('IMPORTATEUR')")
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
    public ResponseEntity<DemandeResponse> creerDemande(@Valid @RequestBody DemandeRequest request) {
        DemandeResponse response = demandeService.creerDemande(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mes-demandes")
<<<<<<< HEAD
    @PreAuthorize("hasRole('IMPORTATEUR') or hasRole('EXPORTATEUR')")
    public ResponseEntity<List<DemandeResponse>> getMesDemandes() {
        // CORRECTION 2: Méthode corrigée pour gérer importateurs et exportateurs
        List<DemandeResponse> demandes = demandeService.getMesDemandesUtilisateur();
=======
    @PreAuthorize("hasRole('IMPORTATEUR')")
    public ResponseEntity<List<DemandeResponse>> getMesDemandes() {
        List<DemandeResponse> demandes = demandeService.getMesDemandesImportateur();
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
        return ResponseEntity.ok(demandes);
    }

    @GetMapping("/agent")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<List<DemandeResponse>> getDemandesAgent() {
        List<DemandeResponse> demandes = demandeService.getDemandesAgent();
        return ResponseEntity.ok(demandes);
    }

    @PutMapping("/{id}/prendre-en-charge")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<DemandeResponse> prendreEnCharge(@PathVariable Long id) {
        DemandeResponse response = demandeService.prendreEnCharge(id);
        return ResponseEntity.ok(response);
    }
<<<<<<< HEAD

    // CORRECTION 3: Ajouter l'annotation @PutMapping et @PreAuthorize manquantes
    @PutMapping("/{id}/finaliser")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<DemandeResponse> finaliserDossier(
            @PathVariable Long id, // CORRECTION 4: Paramètre correct
            @RequestBody Map<String, Object> finalisationData) {

        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Demande demande = demandeRepository.findById(id) // CORRECTION 5: Utiliser le paramètre
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        // Vérifier autorisation agent
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Agent agent = user.getAgent();
        if (agent == null || !demande.getAgent().getId().equals(agent.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à finaliser cette demande");
        }

        // Calculer décision globale
        List<Marchandise> marchandises = marchandiseRepository.findByDemandeId(id);
        String decisionGlobale = calculerDecisionGlobale(marchandises);

        demande.setDecisionGlobale(decisionGlobale);
        demande.setStatus(StatusDemande.CLOTURE);
        demande.setDateCloture(LocalDateTime.now());

        demande = demandeRepository.save(demande);

        // CORRECTION 6: Appeler la méthode du service pour convertir
        DemandeResponse response = demandeService.convertToResponse(demande);
        return ResponseEntity.ok(response);
    }

    private String calculerDecisionGlobale(List<Marchandise> marchandises) {
        boolean hasNonConforme = false;
        boolean hasConformeAvecReserve = false;

        for (Marchandise marchandise : marchandises) {
            if (marchandise.getAvisMarchandise() != null) {
                switch (marchandise.getAvisMarchandise().getAvis()) {
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
=======
}
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
