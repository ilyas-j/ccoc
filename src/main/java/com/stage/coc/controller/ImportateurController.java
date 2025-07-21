package com.stage.coc.controller;

import com.stage.coc.entity.User;
import com.stage.coc.service.ImportateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/importateurs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImportateurController {

    private final ImportateurService importateurService;

    @PostMapping("/register")
    public ResponseEntity<User> creerImportateur(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String nom,
            @RequestParam String telephone,
            @RequestParam String raisonSociale,
            @RequestParam String adresse,
            @RequestParam String codeDouane,
            @RequestParam String ice) {

        User user = importateurService.creerImportateur(email, password, nom, telephone,
                raisonSociale, adresse, codeDouane, ice);
        return ResponseEntity.ok(user);
    }
}
