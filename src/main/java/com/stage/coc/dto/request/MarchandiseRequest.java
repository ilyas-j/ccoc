package com.stage.coc.dto.request;

import com.stage.coc.enums.CategorieMarchandise;
import com.stage.coc.enums.UniteQuantite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarchandiseRequest {
    @NotNull(message = "Catégorie est obligatoire")
    private CategorieMarchandise categorie;

    @NotNull(message = "Quantité est obligatoire")
    @Positive(message = "Quantité doit être positive")
    private BigDecimal quantite;

    @NotNull(message = "Unité de quantité est obligatoire")
    private UniteQuantite uniteQuantite;

    @NotNull(message = "Valeur en DH est obligatoire")
    @Positive(message = "Valeur doit être positive")
    private BigDecimal valeurDh;

    @NotBlank(message = "Nom du produit est obligatoire")
    private String nomProduit;

    @NotBlank(message = "Fabricant est obligatoire")
    private String fabricant;

    @NotBlank(message = "Adresse du fabricant est obligatoire")
    private String adresseFabricant;

    @NotBlank(message = "Pays d'origine est obligatoire")
    private String paysOrigine;
}
