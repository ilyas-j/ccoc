package com.stage.coc.dto.request;

import com.stage.coc.enums.TypeUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    private String telephone;

    @NotNull(message = "Le type d'utilisateur est obligatoire")
    private TypeUser typeUser;

    // Champs pour Importateur
    private String raisonSociale;
    private String adresse;
    private String codeDouane;
    private String ice;

    // Champs pour Exportateur
    private String pays;
    private String ville;
    private String codePostal;
    private String numeroExportateur;
    private String secteurActivite;
    private String numeroRegistre;
    private String ifu;

    // Champs pour Agent
    private Long bureauControleId;
    private boolean superviseur = false;
}