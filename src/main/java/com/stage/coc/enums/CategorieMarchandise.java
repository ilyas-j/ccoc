package com.stage.coc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CategorieMarchandise {
    PRODUITS_INDUSTRIELS_ET_TECHNIQUES("Produits industriels et techniques"),
    VEHICULES_ET_PIECES_DETACHEES("Véhicules et pièces détachées"),
    EQUIPEMENTS_ECLAIRAGE("Équipements d'éclairage"),
    JOUETS_ET_ARTICLES_ENFANTS("Jouets et articles pour enfants"),
    MATERIEL_ELECTRIQUE_ET_CABLAGE("Matériel électrique et câblage"),
    MEUBLES_ET_ARTICLES_BOIS("Meubles et articles en bois"),
    PRODUITS_AGROALIMENTAIRES("Produits agroalimentaires"),
    PRODUITS_PHARMACEUTIQUES_ET_COSMETIQUES("Produits pharmaceutiques et cosmétiques"),
    TEXTILE_ET_HABILLEMENT("Textile et habillement"),
    EQUIPEMENTS_INFORMATIQUES_ET_TELECOMMUNICATION("Équipements informatiques et télécommunication"),
    PRODUITS_CHIMIQUES("Produits chimiques"),
    EMBALLAGES_ET_PRODUITS_PLASTIQUE("Emballages et produits plastique"),
    PRODUITS_PETROLIERS_ET_LUBRIFIANTS("Produits pétroliers et lubrifiants"),
    PRODUITS_CULTURELS_ET_EDUCATIFS("Produits culturels et éducatifs"),
    PRODUITS_AGRICOLES_ANIMAUX_VIVANTS("Produits agricoles et animaux vivants");

    private final String displayName;

    CategorieMarchandise(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static CategorieMarchandise fromString(String value) {
        if (value == null) {
            return null;
        }

        // Normaliser la valeur d'entrée
        String normalizedValue = value.trim();

        // D'abord essayer de trouver par nom exact de l'enum
        for (CategorieMarchandise categorie : CategorieMarchandise.values()) {
            if (categorie.name().equals(normalizedValue)) {
                return categorie;
            }
        }

        // Ensuite essayer de trouver par display name
        for (CategorieMarchandise categorie : CategorieMarchandise.values()) {
            if (categorie.displayName.equals(normalizedValue)) {
                return categorie;
            }
        }

        // Si pas trouvé, lancer une exception avec la liste des valeurs acceptées
        throw new IllegalArgumentException("Catégorie de marchandise non reconnue: " + value +
                ". Valeurs acceptées: " + java.util.Arrays.toString(CategorieMarchandise.values()));
    }

    @Override
    public String toString() {
        return displayName;
    }
}