package com.stage.coc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UniteQuantite {
    PIECE("pièce"),
    KG("kg"),
    TONNE("tonne"),
    LITRE("litre"),
    M2("m²"),
    M3("m³"),
    CARTON("carton"),
    PALETTE("palette");

    private final String displayName;

    UniteQuantite(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static UniteQuantite fromString(String value) {
        if (value == null) {
            return null;
        }

        // Normaliser la valeur d'entrée (supprimer espaces, convertir en minuscules)
        String normalizedValue = value.trim().toLowerCase();

        for (UniteQuantite unite : UniteQuantite.values()) {
            // Comparer avec le nom de l'enum (en minuscules)
            if (unite.name().toLowerCase().equals(normalizedValue)) {
                return unite;
            }
            // Comparer avec le nom d'affichage (en minuscules)
            if (unite.displayName.toLowerCase().equals(normalizedValue)) {
                return unite;
            }
        }

        // Si aucune correspondance trouvée, essayer des correspondances spéciales
        switch (normalizedValue) {
            case "piece":
            case "pièce":
            case "pieces":
            case "pièces":
                return PIECE;
            case "kilogramme":
            case "kilogrammes":
                return KG;
            case "tonnes":
                return TONNE;
            case "litres":
                return LITRE;
            case "metre_carre":
            case "mètre_carré":
            case "m2":
            case "m²":
                return M2;
            case "metre_cube":
            case "mètre_cube":
            case "m3":
            case "m³":
                return M3;
            case "cartons":
                return CARTON;
            case "palettes":
                return PALETTE;
            default:
                throw new IllegalArgumentException("Unité de quantité non reconnue: " + value +
                        ". Valeurs acceptées: " + java.util.Arrays.toString(UniteQuantite.values()));
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}