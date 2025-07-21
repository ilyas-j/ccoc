package com.stage.coc.controller;

import com.stage.coc.dto.request.AvisMarchandiseRequest;
<<<<<<< HEAD
import com.stage.coc.dto.response.DemandeResponse;
=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
import com.stage.coc.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

<<<<<<< HEAD
import java.util.List;

=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AgentController {

    private final AgentService agentService;

<<<<<<< HEAD
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
=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
    @PostMapping("/avis-marchandise")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<Void> donnerAvisMarchandise(@Valid @RequestBody AvisMarchandiseRequest request) {
        agentService.donnerAvisMarchandise(request);
        return ResponseEntity.ok().build();
    }
<<<<<<< HEAD

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
=======
}

>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
