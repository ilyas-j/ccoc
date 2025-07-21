package com.stage.coc.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "exportateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exportateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raison_sociale", nullable = false)
    private String raisonSociale;

    private String telephone;

    private String email;

    private String adresse;

    private String pays;

    private String ifu;

    @OneToOne
    @JoinColumn(name = "user_id") // ✅ AJOUTER CETTE RELATION
    private User user;

    @OneToMany(mappedBy = "exportateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Demande> demandes;
}
