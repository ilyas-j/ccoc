package com.stage.coc.entity;

import com.stage.coc.enums.CategorieMarchandise;
import com.stage.coc.enums.UniteQuantite;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "marchandises")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Marchandise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CategorieMarchandise categorie;

    private BigDecimal quantite;

    @Enumerated(EnumType.STRING)
    @Column(name = "unite_quantite")
    private UniteQuantite uniteQuantite;

    @Column(name = "valeur_dh")
    private BigDecimal valeurDh;

    @Column(name = "nom_produit")
    private String nomProduit;

    private String fabricant;

    @Column(name = "adresse_fabricant")
    private String adresseFabricant;

    @Column(name = "pays_origine")
    private String paysOrigine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id")
    private Demande demande;

    @OneToOne(mappedBy = "marchandise", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private com.stage.coc.entity.AvisMarchandise avisMarchandise;
}