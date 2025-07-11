package com.stage.coc.controller;

import com.stage.coc.dto.request.AvisMarchandiseRequest;
import com.stage.coc.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/avis-marchandise")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<Void> donnerAvisMarchandise(@Valid @RequestBody AvisMarchandiseRequest request) {
        agentService.donnerAvisMarchandise(request);
        return ResponseEntity.ok().build();
    }
}

