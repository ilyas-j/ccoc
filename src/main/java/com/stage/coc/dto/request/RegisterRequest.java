package com.stage.coc.dto.request;

import com.stage.coc.enums.TypeUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Nom est obligatoire")
    private String nom;

    @NotBlank(message = "Email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    private String telephone;

    @NotNull(message = "Type d'utilisateur est obligatoire")
    private TypeUser typeUser;

    // Champs spécifiques à l'importateur
    private String raisonSociale;
    private String adresse;
    private String codeDouane;
    private String ice;

    // Champs spécifiques à l'agent
    private Long bureauControleId;
    private boolean superviseur = false;
}
