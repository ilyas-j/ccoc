package com.stage.coc.controller;

import com.stage.coc.dto.request.DemandeRequest;
import com.stage.coc.dto.response.DemandeResponse;
import com.stage.coc.service.DemandeService;
import com.stage.coc.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DemandeController {

    private final DemandeService demandeService;
    private final AgentService agentService;

    @PostMapping
    @PreAuthorize("hasRole('IMPORTATEUR') or hasRole('EXPORTATEUR')")
    public ResponseEntity<DemandeResponse> creerDemande(@Valid @RequestBody DemandeRequest request) {
        DemandeResponse response = demandeService.creerDemande(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mes-demandes")
    @PreAuthorize("hasRole('IMPORTATEUR') or hasRole('EXPORTATEUR')")
    public ResponseEntity<List<DemandeResponse>> getMesDemandes() {
        List<DemandeResponse> demandes = demandeService.getMesDemandesUtilisateur();
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

    @PutMapping("/{id}/finaliser")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<DemandeResponse> finaliserDossier(@PathVariable Long id) {
        // Delegate to the AgentService which has the finaliserDossier method
        DemandeResponse response = agentService.finaliserDossier(id);
        return ResponseEntity.ok(response);
    }
}