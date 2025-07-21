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

<<<<<<< HEAD
    @OneToOne
    @JoinColumn(name = "user_id") // ✅ AJOUTER CETTE RELATION
    private User user;

=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
    @OneToMany(mappedBy = "exportateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Demande> demandes;
}
