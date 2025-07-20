package com.stage.coc.dto.response;

import com.stage.coc.enums.StatusDemande;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DemandeResponse {
    private Long id;
    private LocalDateTime dateAffectation; // AJOUTER
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
    private String delaiEstime;
}