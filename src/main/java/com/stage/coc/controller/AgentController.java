package com.stage.coc.controller;

import com.stage.coc.dto.request.AvisMarchandiseRequest;
import com.stage.coc.dto.response.DemandeResponse;
import com.stage.coc.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AgentController {

    private final AgentService agentService;

    /**
     * Récupérer les demandes affectées à l'agent connecté
     */
    @GetMapping("/demandes")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<List<DemandeResponse>> getDemandesAffectees() {
        List<DemandeResponse> demandes = agentService.getDemandesAffectees();
        return ResponseEntity.ok(demandes);
    }

    /**
     * Prendre en charge une demande (passer de DEPOSE à EN_COURS_DE_TRAITEMENT)
     */
    @PutMapping("/demandes/{demandeId}/prendre-en-charge")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<DemandeResponse> prendreEnCharge(@PathVariable Long demandeId) {
        DemandeResponse response = agentService.prendreEnCharge(demandeId);
        return ResponseEntity.ok(response);
    }

    /**
     * Donner un avis sur une marchandise
     */
    @PostMapping("/avis-marchandise")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<Void> donnerAvisMarchandise(@Valid @RequestBody AvisMarchandiseRequest request) {
        agentService.donnerAvisMarchandise(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Finaliser un dossier (calculer décision globale et clôturer)
     */
    @PutMapping("/demandes/{demandeId}/finaliser")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<DemandeResponse> finaliserDossier(@PathVariable Long demandeId) {
        DemandeResponse response = agentService.finaliserDossier(demandeId);
        return ResponseEntity.ok(response);
    }
}