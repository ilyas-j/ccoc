package com.stage.coc.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "bureaux_controle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BureauControle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom; // TUV, ECF, AFNOR, ICUM, SGS

    private String adresse;

    private String telephone;

    private String email;

    @OneToMany(mappedBy = "bureauControle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Agent> agents;

    @OneToMany(mappedBy = "bureauControle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Demande> demandes;
}
