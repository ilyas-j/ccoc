package com.stage.coc.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DemandeRequest {
<<<<<<< HEAD
    // Informations importateur (utilisées quand c'est l'exportateur qui fait la demande)
=======
    // Informations importateur
    @NotBlank(message = "Nom/Raison sociale importateur est obligatoire")
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
    private String importateurNom;
    private String importateurTelephone;
    @Email(message = "Email importateur invalide")
    private String importateurEmail;
    private String importateurAdresse;
    private String importateurCodeDouane;
    private String importateurIce;

<<<<<<< HEAD
    // Informations exportateur (utilisées quand c'est l'importateur qui fait la demande)
=======
    // Informations exportateur
    @NotBlank(message = "Nom/Raison sociale exportateur est obligatoire")
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
    private String exportateurNom;
    private String exportateurTelephone;
    @Email(message = "Email exportateur invalide")
    private String exportateurEmail;
    private String exportateurAdresse;
<<<<<<< HEAD
=======
    @NotBlank(message = "Pays exportateur est obligatoire")
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
    private String exportateurPays;
    private String exportateurIfu;

    // Liste des marchandises
    @NotEmpty(message = "Au moins une marchandise est requise")
    @Valid
    private List<MarchandiseRequest> marchandises;
}