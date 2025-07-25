package com.stage.coc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "superviseurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Superviseur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bureau_controle_id")
    private BureauControle bureauControle;

    // Permissions spécifiques superviseur
    private boolean peutReaffecter = true;
    private boolean peutGererAgents = true;
    private boolean peutVoirToutesLesDemandes = true;

    // Peut aussi traiter des demandes comme un agent
    private boolean peutTraiterDemandes = true;
    private int chargeTravailPersonnelle = 0;
    private boolean disponiblePourTraitement = true;
}
