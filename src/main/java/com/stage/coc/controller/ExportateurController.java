package com.stage.coc.controller;

import com.stage.coc.entity.User;
import com.stage.coc.service.ExportateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exportateurs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExportateurController {

    private final ExportateurService exportateurService;

    @PostMapping("/register")
    public ResponseEntity<User> creerExportateur(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String nom,
            @RequestParam String telephone,
            @RequestParam String raisonSociale,
            @RequestParam String adresse,
            @RequestParam String pays,
            @RequestParam String ifu) {

        User user = exportateurService.creerExportateur(email, password, nom, telephone,
                raisonSociale, adresse, pays, ifu);
        return ResponseEntity.ok(user);
    }
}