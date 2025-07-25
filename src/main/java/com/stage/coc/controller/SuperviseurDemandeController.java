package com.stage.coc.controller;

import com.stage.coc.dto.request.AvisMarchandiseRequest;
import com.stage.coc.dto.response.DemandeResponse;
import com.stage.coc.service.AgentService;
import com.stage.coc.service.SuperviseurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superviseur/traitement")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SuperviseurDemandeController {

    private final AgentService agentService; // Réutiliser la logique agent
    private final SuperviseurService superviseurService;

    /**
     * Le superviseur peut traiter des demandes comme un agent
     */
    @GetMapping("/demandes")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<List<DemandeResponse>> getDemandesATraiter() {
        List<DemandeResponse> demandes = superviseurService.getMesDemandesPersonnelles();
        return ResponseEntity.ok(demandes);
    }

    /**
     * Prendre en charge (comme un agent)
     */
    @PutMapping("/demandes/{demandeId}/prendre-en-charge")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<DemandeResponse> prendreEnCharge(@PathVariable Long demandeId) {
        DemandeResponse response = agentService.prendreEnCharge(demandeId);
        return ResponseEntity.ok(response);
    }

    /**
     * Donner avis marchandise (comme un agent)
     */
    @PostMapping("/avis-marchandise")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<Void> donnerAvisMarchandise(@Valid @RequestBody AvisMarchandiseRequest request) {
        agentService.donnerAvisMarchandise(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Finaliser dossier (comme un agent)
     */
    @PutMapping("/demandes/{demandeId}/finaliser")
    @PreAuthorize("hasRole('SUPERVISEUR')")
    public ResponseEntity<DemandeResponse> finaliserDossier(@PathVariable Long demandeId) {
        DemandeResponse response = agentService.finaliserDossier(demandeId);
        return ResponseEntity.ok(response);
    }
}