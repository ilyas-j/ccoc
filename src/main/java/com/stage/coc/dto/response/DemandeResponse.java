package com.stage.coc.dto.response;

import com.stage.coc.enums.StatusDemande;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DemandeResponse {
    private Long id;
<<<<<<< HEAD
    private LocalDateTime dateAffectation; // AJOUTER
=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
    private String numeroDemande;
    private StatusDemande status;
    private LocalDateTime dateCreation;
    private LocalDateTime dateTraitement;
    private LocalDateTime dateCloture;
    private String decisionGlobale;
    private String importateurNom;
    private String exportateurNom;
    private String bureauControleNom;
    private String agentNom;
    private List<MarchandiseResponse> marchandises;
<<<<<<< HEAD
    private String delaiEstime;
=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
}