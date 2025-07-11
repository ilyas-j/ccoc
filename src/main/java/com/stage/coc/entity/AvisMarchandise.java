package com.stage.coc.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "avis_marchandises")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvisMarchandise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private com.stage.coc.enums.AvisMarchandise avis;

    private String commentaire;

    @OneToOne
    @JoinColumn(name = "marchandise_id")
    private Marchandise marchandise;
}