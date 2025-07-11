package com.stage.coc.dto.response;

import com.stage.coc.enums.CategorieMarchandise;
import com.stage.coc.enums.UniteQuantite;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarchandiseResponse {
    private Long id;
    private CategorieMarchandise categorie;
    private BigDecimal quantite;
    private UniteQuantite uniteQuantite;
    private BigDecimal valeurDh;
    private String nomProduit;
    private String fabricant;
    private String adresseFabricant;
    private String paysOrigine;
    private String avis;
    private String commentaire;
}