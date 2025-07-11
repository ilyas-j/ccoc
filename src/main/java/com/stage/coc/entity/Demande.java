package com.stage.coc.entity;

import com.stage.coc.enums.StatusDemande;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "demandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Demande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_demande", unique = true)
    private String numeroDemande;

    @Enumerated(EnumType.STRING)
    private StatusDemande status = StatusDemande.DEPOSE;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;

    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;

    @Column(name = "decision_globale")
    private String decisionGlobale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importateur_id")
    private Importateur importateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exportateur_id")
    private Exportateur exportateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bureau_controle_id")
    private BureauControle bureauControle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent agent;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Marchandise> marchandises;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        if (numeroDemande == null) {
            numeroDemande = "COC-" + System.currentTimeMillis();
        }
    }
}