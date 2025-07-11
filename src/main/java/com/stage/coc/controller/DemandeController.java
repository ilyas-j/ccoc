package com.stage.coc.controller;

import com.stage.coc.dto.request.DemandeRequest;
import com.stage.coc.dto.response.DemandeResponse;
import com.stage.coc.service.DemandeService;
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

    @PostMapping
    @PreAuthorize("hasRole('IMPORTATEUR')")
    public ResponseEntity<DemandeResponse> creerDemande(@Valid @RequestBody DemandeRequest request) {
        DemandeResponse response = demandeService.creerDemande(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mes-demandes")
    @PreAuthorize("hasRole('IMPORTATEUR')")
    public ResponseEntity<List<DemandeResponse>> getMesDemandes() {
        List<DemandeResponse> demandes = demandeService.getMesDemandesImportateur();
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
}
