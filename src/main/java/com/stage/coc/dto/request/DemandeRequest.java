package com.stage.coc.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DemandeRequest {
    // Informations importateur
    @NotBlank(message = "Nom/Raison sociale importateur est obligatoire")
    private String importateurNom;
    private String importateurTelephone;
    @Email(message = "Email importateur invalide")
    private String importateurEmail;
    private String importateurAdresse;
    private String importateurCodeDouane;
    private String importateurIce;

    // Informations exportateur
    @NotBlank(message = "Nom/Raison sociale exportateur est obligatoire")
    private String exportateurNom;
    private String exportateurTelephone;
    @Email(message = "Email exportateur invalide")
    private String exportateurEmail;
    private String exportateurAdresse;
    @NotBlank(message = "Pays exportateur est obligatoire")
    private String exportateurPays;
    private String exportateurIfu;

    // Liste des marchandises
    @NotEmpty(message = "Au moins une marchandise est requise")
    @Valid
    private List<MarchandiseRequest> marchandises;
}