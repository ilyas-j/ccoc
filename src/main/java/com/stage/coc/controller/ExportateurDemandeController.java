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
@RequestMapping("/api/exportateur")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExportateurDemandeController {

    private final DemandeService demandeService;

    @GetMapping("/demandes")
    @PreAuthorize("hasRole('EXPORTATEUR')")
    public ResponseEntity<List<DemandeResponse>> getMesDemandesExportateur() {
        List<DemandeResponse> demandes = demandeService.getMesDemandesUtilisateur();
        return ResponseEntity.ok(demandes);
    }

    @PostMapping("/demandes")
    @PreAuthorize("hasRole('EXPORTATEUR')")
    public ResponseEntity<DemandeResponse> creerDemandeExportateur(@Valid @RequestBody DemandeRequest request) {
        DemandeResponse response = demandeService.creerDemande(request);
        return ResponseEntity.ok(response);
    }
}