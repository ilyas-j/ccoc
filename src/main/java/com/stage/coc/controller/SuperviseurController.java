package com.stage.coc.controller;

import com.stage.coc.dto.response.DemandeResponse;
import com.stage.coc.entity.Agent;
import com.stage.coc.service.SuperviseurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superviseur")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SuperviseurController {

    private final SuperviseurService superviseurService;

    @GetMapping("/demandes")
    @PreAuthorize("hasRole('AGENT')") // Superviseur est aussi un agent
    public ResponseEntity<List<DemandeResponse>> getDemandesBureau() {
        List<DemandeResponse> demandes = superviseurService.getDemandesBureau();
        return ResponseEntity.ok(demandes);
    }

    @GetMapping("/agents")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<List<Agent>> getAgentsBureau() {
        List<Agent> agents = superviseurService.getAgentsBureau();
        return ResponseEntity.ok(agents);
    }

    @PutMapping("/demandes/{demandeId}/reaffecter/{agentId}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<Void> reaffecterDemande(@PathVariable Long demandeId, @PathVariable Long agentId) {
        superviseurService.reaffecterDemande(demandeId, agentId);
        return ResponseEntity.ok().build();
    }
}