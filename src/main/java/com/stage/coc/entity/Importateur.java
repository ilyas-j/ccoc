package com.stage.coc.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "importateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Importateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raison_sociale")
    private String raisonSociale;

    private String adresse;

    @Column(name = "code_douane")
    private String codeDouane;

    private String ice;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "importateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Demande> demandes;
}